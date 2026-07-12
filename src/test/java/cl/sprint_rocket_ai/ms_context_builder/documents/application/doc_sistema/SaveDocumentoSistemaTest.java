package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_sistema;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoSistema;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoSistemaMongoRepository;
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
class SaveDocumentoSistemaTest {

    @Mock
    private DocumentoSistemaMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private SaveDocumentoSistema saveDocumentoSistema;

    private DocumentoSistemaRequest buildRequest() {
        return new DocumentoSistemaRequest(
                "Servicio de Pagos",
                "Microservicio que gestiona transacciones.",
                List.of("https://github.com/org/pagos"),
                List.of("Java", "Spring Boot", "Kafka"),
                List.of("pagos", "transacciones"),
                List.of("dev1", "dev2")
        );
    }

    @Test
    @DisplayName("Debe guardar el documento sistema e indexarlo cuando el request es válido")
    void shouldSaveDocumentoSistemaAndIndexWhenRequestIsValid() {
        // Given
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> {
            DocumentoSistema doc = inv.getArgument(0);
            doc.setId("sis-001");
            return doc;
        });

        // When
        DocumentoSistemaResponse result = saveDocumentoSistema.execute(buildRequest());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("sis-001");
        assertThat(result.titulo()).isEqualTo("Servicio de Pagos");
    }

    @Test
    @DisplayName("Debe asignar todos los campos del request al documento antes de persistir")
    void shouldSetAllFieldsFromRequestWhenSaving() {
        // Given
        ArgumentCaptor<DocumentoSistema> captor = ArgumentCaptor.forClass(DocumentoSistema.class);
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoSistema.execute(buildRequest());

        // Then
        verify(repository).save(captor.capture());
        DocumentoSistema captured = captor.getValue();
        assertThat(captured.getTitulo()).isEqualTo("Servicio de Pagos");
        assertThat(captured.getContenido()).isEqualTo("Microservicio que gestiona transacciones.");
        assertThat(captured.getStack()).containsExactly("Java", "Spring Boot", "Kafka");
        assertThat(captured.getUrlRepos()).containsExactly("https://github.com/org/pagos");
        assertThat(captured.getDevs()).containsExactly("dev1", "dev2");
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación antes de persistir")
    void shouldSetFechaCreacionWhenSaving() {
        // Given
        ArgumentCaptor<DocumentoSistema> captor = ArgumentCaptor.forClass(DocumentoSistema.class);
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoSistema.execute(buildRequest());

        // Then
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Debe llamar a aiIndexService.index después de persistir el documento")
    void shouldCallAiIndexServiceAfterPersistence() {
        // Given
        when(repository.save(any(DocumentoSistema.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoSistema.execute(buildRequest());

        // Then
        InOrder inOrder = inOrder(repository, aiIndexService);
        inOrder.verify(repository).save(any(DocumentoSistema.class));
        inOrder.verify(aiIndexService).index(any(DocumentoSistema.class));
    }
}
