package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_sistema;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoSistema;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoSistemaMongoRepository;
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
class UpdateDocumentoSistemaTest {

    @Mock
    private DocumentoSistemaMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private UpdateDocumentoSistema updateDocumentoSistema;

    private DocumentoSistemaRequest buildRequest() {
        return new DocumentoSistemaRequest(
                "Servicio de Pagos v2",
                "Versión actualizada del microservicio.",
                List.of("https://github.com/org/pagos-v2"),
                List.of("Java", "Quarkus"),
                List.of("pagos"),
                List.of("dev3")
        );
    }

    private DocumentoSistema buildExistingDoc(String id) {
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId(id);
        doc.setTitulo("Servicio de Pagos v1");
        doc.setFechaCreacion(LocalDateTime.now().minusDays(1));
        return doc;
    }

    @Test
    @DisplayName("Debe actualizar el documento y reindexarlo cuando el id existe")
    void shouldUpdateAndReindexWhenDocumentoExists() {
        // Given
        String id = "sis-001";
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        DocumentoSistemaResponse result = updateDocumentoSistema.execute(id, buildRequest());

        // Then
        assertThat(result.titulo()).isEqualTo("Servicio de Pagos v2");
        verify(repository).save(any(DocumentoSistema.class));
        verify(aiIndexService).update(any(DocumentoSistema.class));
    }

    @Test
    @DisplayName("Debe asignar todos los campos del request al documento al actualizar")
    void shouldSetAllFieldsFromRequestWhenUpdating() {
        // Given
        String id = "sis-002";
        ArgumentCaptor<DocumentoSistema> captor = ArgumentCaptor.forClass(DocumentoSistema.class);
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        updateDocumentoSistema.execute(id, buildRequest());

        // Then
        verify(repository).save(captor.capture());
        DocumentoSistema captured = captor.getValue();
        assertThat(captured.getTitulo()).isEqualTo("Servicio de Pagos v2");
        assertThat(captured.getStack()).containsExactly("Java", "Quarkus");
        assertThat(captured.getDevs()).containsExactly("dev3");
    }

    @Test
    @DisplayName("Debe establecer la fecha de actualización al persistir")
    void shouldSetFechaActualizacionWhenUpdating() {
        // Given
        String id = "sis-003";
        ArgumentCaptor<DocumentoSistema> captor = ArgumentCaptor.forClass(DocumentoSistema.class);
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        updateDocumentoSistema.execute(id, buildRequest());

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
        assertThatThrownBy(() -> updateDocumentoSistema.execute(id, buildRequest()))
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
        assertThatThrownBy(() -> updateDocumentoSistema.execute(id, buildRequest()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(aiIndexService, never()).update(any());
        verify(repository, never()).save(any());
    }
}
