package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_negocio;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoNegocio;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoNegocioMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveDocumentoNegocioTest {

    @Mock
    private DocumentoNegocioMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private SaveDocumentoNegocio saveDocumentoNegocio;

    private DocumentoNegocioRequest buildRequest() {
        return new DocumentoNegocioRequest(
                "Alta de Cliente",
                "El cliente completa el formulario de registro.",
                List.of("onboarding"),
                List.of("Se valida RUT", "Se envía email de confirmación")
        );
    }

    @Test
    @DisplayName("Debe guardar el documento negocio e indexarlo cuando el request es válido")
    void shouldSaveDocumentoNegocioAndIndexWhenRequestIsValid() {
        // Given
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> {
            DocumentoNegocio doc = inv.getArgument(0);
            doc.setId("neg-001");
            return doc;
        });

        // When
        DocumentoNegocioResponse result = saveDocumentoNegocio.execute(buildRequest());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("neg-001");
        assertThat(result.titulo()).isEqualTo("Alta de Cliente");
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación antes de persistir")
    void shouldSetFechaCreacionWhenSaving() {
        // Given
        ArgumentCaptor<DocumentoNegocio> captor = ArgumentCaptor.forClass(DocumentoNegocio.class);
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoNegocio.execute(buildRequest());

        // Then
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar DocumentoNegocioResponse mapeado desde el documento guardado")
    void shouldReturnDocumentoNegocioResponseWhenSaveSucceeds() {
        // Given
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> {
            DocumentoNegocio doc = inv.getArgument(0);
            doc.setId("neg-003");
            return doc;
        });

        // When
        DocumentoNegocioResponse result = saveDocumentoNegocio.execute(buildRequest());

        // Then
        assertThat(result.id()).isEqualTo("neg-003");
        assertThat(result.criteriosAceptacion()).containsExactly("Se valida RUT", "Se envía email de confirmación");
        assertThat(result.tags()).containsExactly("onboarding");
    }

    @Test
    @DisplayName("Debe llamar a aiIndexService.index después de persistir el documento")
    void shouldCallAiIndexServiceAfterPersistence() {
        // Given
        when(repository.save(any(DocumentoNegocio.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoNegocio.execute(buildRequest());

        // Then
        InOrder inOrder = inOrder(repository, aiIndexService);
        inOrder.verify(repository).save(any(DocumentoNegocio.class));
        inOrder.verify(aiIndexService).index(any(DocumentoNegocio.class));
    }
}
