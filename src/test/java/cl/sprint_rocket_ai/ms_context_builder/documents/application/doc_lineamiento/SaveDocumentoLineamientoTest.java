package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_lineamiento;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoLineamiento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoLineamientoMongoRepository;
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
class SaveDocumentoLineamientoTest {

    @Mock
    private DocumentoLineamientoMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private SaveDocumentoLineamiento saveDocumentoLineamiento;

    private DocumentoLineamientoRequest buildRequest() {
        return new DocumentoLineamientoRequest(
                "Lineamiento de Seguridad",
                "Toda API debe requerir autenticación JWT.",
                List.of("seguridad", "api")
        );
    }

    @Test
    @DisplayName("Debe guardar el documento lineamiento e indexarlo cuando el request es válido")
    void shouldSaveDocumentoLineamientoAndIndexWhenRequestIsValid() {
        // Given
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> {
            DocumentoLineamiento doc = inv.getArgument(0);
            doc.setId("lin-001");
            return doc;
        });

        // When
        DocumentoLineamientoResponse result = saveDocumentoLineamiento.execute(buildRequest());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("lin-001");
        assertThat(result.titulo()).isEqualTo("Lineamiento de Seguridad");
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación antes de persistir")
    void shouldSetFechaCreacionWhenSaving() {
        // Given
        ArgumentCaptor<DocumentoLineamiento> captor = ArgumentCaptor.forClass(DocumentoLineamiento.class);
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoLineamiento.execute(buildRequest());

        // Then
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar DocumentoLineamientoResponse mapeado desde el documento guardado")
    void shouldReturnDocumentoLineamientoResponseWhenSaveSucceeds() {
        // Given
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> {
            DocumentoLineamiento doc = inv.getArgument(0);
            doc.setId("lin-003");
            return doc;
        });

        // When
        DocumentoLineamientoResponse result = saveDocumentoLineamiento.execute(buildRequest());

        // Then
        assertThat(result.id()).isEqualTo("lin-003");
        assertThat(result.contenido()).isEqualTo("Toda API debe requerir autenticación JWT.");
        assertThat(result.tags()).containsExactly("seguridad", "api");
    }

    @Test
    @DisplayName("Debe llamar a aiIndexService.index después de persistir el documento")
    void shouldCallAiIndexServiceAfterPersistence() {
        // Given
        when(repository.save(any(DocumentoLineamiento.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoLineamiento.execute(buildRequest());

        // Then
        InOrder inOrder = inOrder(repository, aiIndexService);
        inOrder.verify(repository).save(any(DocumentoLineamiento.class));
        inOrder.verify(aiIndexService).index(any(DocumentoLineamiento.class));
    }
}
