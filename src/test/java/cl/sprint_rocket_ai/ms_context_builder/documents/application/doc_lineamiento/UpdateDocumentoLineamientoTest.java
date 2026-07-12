package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_lineamiento;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoLineamiento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoLineamientoMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDocumentoLineamientoTest {

    @Mock
    private DocumentoLineamientoMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private UpdateDocumentoLineamiento updateDocumentoLineamiento;

    private DocumentoLineamientoRequest buildRequest() {
        return new DocumentoLineamientoRequest(
                "Lineamiento Actualizado",
                "Contenido actualizado.",
                List.of("nuevo-tag")
        );
    }

    private DocumentoLineamiento buildExistingDoc(String id) {
        DocumentoLineamiento doc = new DocumentoLineamiento();
        doc.setId(id);
        doc.setTitulo("Lineamiento Original");
        doc.setContenido("Contenido original.");
        doc.setFechaCreacion(LocalDateTime.now().minusDays(1));
        return doc;
    }

    @Test
    @DisplayName("Debe actualizar el documento y reindexarlo cuando el id existe")
    void shouldUpdateAndReindexWhenDocumentoExists() {
        // Given
        String id = "lin-001";
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        DocumentoLineamientoResponse result = updateDocumentoLineamiento.execute(id, buildRequest());

        // Then
        assertThat(result.titulo()).isEqualTo("Lineamiento Actualizado");
        verify(repository).save(any(DocumentoLineamiento.class));
        verify(aiIndexService).update(any(DocumentoLineamiento.class));
    }

    @Test
    @DisplayName("Debe establecer la fecha de actualización al persistir")
    void shouldSetFechaActualizacionWhenUpdating() {
        // Given
        String id = "lin-002";
        ArgumentCaptor<DocumentoLineamiento> captor = ArgumentCaptor.forClass(DocumentoLineamiento.class);
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        updateDocumentoLineamiento.execute(id, buildRequest());

        // Then
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaActualizacion()).isNotNull();
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException cuando el documento no existe")
    void shouldThrowEntityNotFoundExceptionWhenDocumentoNotExists() {
        // Given
        String id = "id-inexistente";
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> updateDocumentoLineamiento.execute(id, buildRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id);
    }

    @Test
    @DisplayName("No debe llamar a aiIndexService cuando el documento no existe")
    void shouldNotCallAiIndexWhenDocumentoNotFound() {
        // Given
        String id = "id-inexistente";
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> updateDocumentoLineamiento.execute(id, buildRequest()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(aiIndexService, never()).update(any());
        verify(repository, never()).save(any());
    }
}
