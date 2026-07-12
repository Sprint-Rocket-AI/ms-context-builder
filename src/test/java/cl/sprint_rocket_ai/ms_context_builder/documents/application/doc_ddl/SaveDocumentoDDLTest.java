package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_ddl;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.MotorBaseDatos;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoDDL;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.ColumnaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.DocumentoDDLRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.DocumentoDDLResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.TablaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoDDLMongoRepository;
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
class SaveDocumentoDDLTest {

    @Mock
    private DocumentoDDLMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private SaveDocumentoDDL saveDocumentoDDL;

    private DocumentoDDLRequest buildRequest() {
        ColumnaRequest columna = new ColumnaRequest("id", "BIGINT", true, false, false, true, null, "PK");
        TablaRequest tabla = new TablaRequest("usuarios", "public", "Tabla de usuarios", List.of(columna), List.of());
        return new DocumentoDDLRequest("Schema Usuarios", "CREATE TABLE usuarios (id BIGINT);",
                MotorBaseDatos.POSTGRESQL, "1.0", List.of(tabla));
    }

    @Test
    @DisplayName("Debe guardar el documento DDL e indexarlo cuando el request es válido")
    void shouldSaveDocumentoDDLAndIndexWhenRequestIsValid() {
        // Given
        DocumentoDDLRequest request = buildRequest();
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> {
            DocumentoDDL doc = inv.getArgument(0);
            doc.setId("ddl-001");
            return doc;
        });

        // When
        DocumentoDDLResponse result = saveDocumentoDDL.execute(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("ddl-001");
        assertThat(result.titulo()).isEqualTo("Schema Usuarios");
    }

    @Test
    @DisplayName("Debe establecer la fecha de creación antes de persistir")
    void shouldSetFechaCreacionWhenSaving() {
        // Given
        DocumentoDDLRequest request = buildRequest();
        ArgumentCaptor<DocumentoDDL> captor = ArgumentCaptor.forClass(DocumentoDDL.class);
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        saveDocumentoDDL.execute(request);

        // Then
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar DocumentoDDLResponse mapeado desde el documento guardado")
    void shouldReturnDocumentoDDLResponseWhenSaveSucceeds() {
        // Given
        DocumentoDDLRequest request = buildRequest();
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> {
            DocumentoDDL doc = inv.getArgument(0);
            doc.setId("ddl-003");
            return doc;
        });

        // When
        DocumentoDDLResponse result = saveDocumentoDDL.execute(request);

        // Then
        assertThat(result.id()).isEqualTo("ddl-003");
        assertThat(result.motorBd()).isEqualTo(MotorBaseDatos.POSTGRESQL);
        assertThat(result.tipo()).isNotNull();
    }

    @Test
    @DisplayName("Debe llamar a aiIndexService.index después de persistir el documento")
    void shouldCallAiIndexServiceAfterPersistence() {
        // Given
        DocumentoDDLRequest request = buildRequest();
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> {
            DocumentoDDL doc = inv.getArgument(0);
            doc.setId("ddl-004");
            return doc;
        });

        // When
        saveDocumentoDDL.execute(request);

        // Then
        InOrder inOrder = inOrder(repository, aiIndexService);
        inOrder.verify(repository).save(any(DocumentoDDL.class));
        inOrder.verify(aiIndexService).index(any(DocumentoDDL.class));
    }
}
