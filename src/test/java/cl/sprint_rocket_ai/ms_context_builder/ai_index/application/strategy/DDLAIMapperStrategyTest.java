package cl.sprint_rocket_ai.ms_context_builder.ai_index.application.strategy;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.models.AIIndexRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoDDL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DDLAIMapperStrategyTest {

    @InjectMocks
    private DDLAIMapperStrategy strategy;

    @Test
    @DisplayName("Debe mapear DocumentoDDL a AIIndexRequest con todos los campos poblados")
    void shouldMapDocumentoDDLToAIIndexRequestWhenFieldsArePopulated() {
        // Given
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId("ddl-001");
        doc.setTitulo("Schema de Usuarios");
        doc.setContenido("CREATE TABLE usuarios (id BIGINT PRIMARY KEY);");

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.id()).isEqualTo("ddl-001");
        assertThat(result.tipo()).isEqualTo(TipoDocumento.DDL.name());
        assertThat(result.contenido()).contains("Schema de Usuarios");
        assertThat(result.contenido()).contains("CREATE TABLE usuarios (id BIGINT PRIMARY KEY);");
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("Debe reemplazar titulo nulo por cadena vacía al mapear DDL")
    void shouldHandleNullTituloWhenMappingDDL() {
        // Given
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId("ddl-002");
        doc.setTitulo(null);
        doc.setContenido("CREATE TABLE log (id INT);");

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).contains("Titulo: ");
        assertThat(result.contenido()).doesNotContain("null");
    }

    @Test
    @DisplayName("Debe reemplazar contenido nulo por cadena vacía al mapear DDL")
    void shouldHandleNullContenidoWhenMappingDDL() {
        // Given
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId("ddl-003");
        doc.setTitulo("Schema Pagos");
        doc.setContenido(null);

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).contains("Schema Pagos");
        assertThat(result.contenido()).doesNotContain("null");
    }

    @Test
    @DisplayName("Debe retornar lista de tags vacía siempre al mapear DDL")
    void shouldReturnEmptyTagsListWhenMappingDDL() {
        // Given
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId("ddl-004");
        doc.setTitulo("Schema");
        doc.setContenido("DDL content");

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("Debe incluir documentoId, tipo y titulo en la metadata al mapear DDL")
    void shouldIncludeTipoDocumentoInMetadataWhenMappingDDL() {
        // Given
        DocumentoDDL doc = new DocumentoDDL();
        doc.setId("ddl-005");
        doc.setTitulo("Schema Inventario");
        doc.setContenido("...");

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.metadata())
                .containsEntry("documentoId", "ddl-005")
                .containsEntry("tipo", TipoDocumento.DDL.name())
                .containsEntry("titulo", "Schema Inventario");
    }
}
