package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_negocio;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoNegocio;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoNegocioMongoRepository;
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
class UpdateDocumentoNegocioTest {

    @Mock
    private DocumentoNegocioMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private UpdateDocumentoNegocio updateDocumentoNegocio;

    private DocumentoNegocioRequest buildRequest() {
        return new DocumentoNegocioRequest(
                "Alta de Cliente v2",
                "Proceso actualizado de registro.",
                List.of("onboarding", "v2"),
                List.of("Criterio actualizado")
        );
    }

    private DocumentoNegocio buildExistingDoc(String id) {
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId(id);
        doc.setTitulo("Alta de Cliente v1");
        doc.setFechaCreacion(LocalDateTime.now().minusDays(1));
        return doc;
    }

    @Test
    @DisplayName("Debe actualizar el documento y reindexarlo cuando el id existe")
    void shouldUpdateAndReindexWhenDocumentoExists() {
        // Given
        String id = "neg-001";
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        DocumentoNegocioResponse result = updateDocumentoNegocio.execute(id, buildRequest());

        // Then
        assertThat(result.titulo()).isEqualTo("Alta de Cliente v2");
        verify(repository).save(any(DocumentoNegocio.class));
        verify(aiIndexService).update(any(DocumentoNegocio.class));
    }

    @Test
    @DisplayName("Debe establecer la fecha de actualización al persistir")
    void shouldSetFechaActualizacionWhenUpdating() {
        // Given
        String id = "neg-002";
        ArgumentCaptor<DocumentoNegocio> captor = ArgumentCaptor.forClass(DocumentoNegocio.class);
        when(repository.findById(id)).thenReturn(Optional.of(buildExistingDoc(id)));
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        updateDocumentoNegocio.execute(id, buildRequest());

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
        assertThatThrownBy(() -> updateDocumentoNegocio.execute(id, buildRequest()))
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
        assertThatThrownBy(() -> updateDocumentoNegocio.execute(id, buildRequest()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(aiIndexService, never()).update(any());
        verify(repository, never()).save(any());
    }
}
