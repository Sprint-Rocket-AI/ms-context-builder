package cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_ddl;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.MotorBaseDatos;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
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
class UpdateDocumentoDDLTest {

    @Mock
    private DocumentoDDLMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private UpdateDocumentoDDL updateDocumentoDDL;

    private DocumentoDDLRequest buildRequest() {
        ColumnaRequest columna = new ColumnaRequest("id", "BIGINT", true, false, false, true, null, "PK");
        TablaRequest tabla = new TablaRequest("pagos", "public", "Tabla de pagos", List.of(columna), List.of());
        return new DocumentoDDLRequest("Schema Pagos v2", "CREATE TABLE pagos (id BIGINT);",
                MotorBaseDatos.MYSQL, "2.0", List.of(tabla));
    }

    private DocumentoDDL buildExistingDoc(String id) {
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId(id);
        doc.setTitulo("Schema Pagos v1");
        doc.setContenido("CREATE TABLE pagos (id INT);");
        doc.setFechaCreacion(LocalDateTime.now().minusDays(1));
        return doc;
    }

    @Test
    @DisplayName("Debe actualizar el documento y reindexarlo cuando el id existe")
    void shouldUpdateAndReindexWhenDocumentoExists() {
        // Given
        String id = "ddl-001";
        DocumentoDDL existing = buildExistingDoc(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        DocumentoDDLResponse result = updateDocumentoDDL.execute(id, buildRequest());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.titulo()).isEqualTo("Schema Pagos v2");
        verify(repository).save(any(DocumentoDDL.class));
        verify(aiIndexService).update(any(DocumentoDDL.class));
    }

    @Test
    @DisplayName("Debe establecer la fecha de actualización al persistir")
    void shouldSetFechaActualizacionWhenUpdating() {
        // Given
        String id = "ddl-002";
        DocumentoDDL existing = buildExistingDoc(id);
        ArgumentCaptor<DocumentoDDL> captor = ArgumentCaptor.forClass(DocumentoDDL.class);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(DocumentoDDL.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        updateDocumentoDDL.execute(id, buildRequest());

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
        assertThatThrownBy(() -> updateDocumentoDDL.execute(id, buildRequest()))
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
        assertThatThrownBy(() -> updateDocumentoDDL.execute(id, buildRequest()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(aiIndexService, never()).update(any());
        verify(repository, never()).save(any());
    }
}
