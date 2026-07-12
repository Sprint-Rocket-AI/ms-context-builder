package cl.sprint_rocket_ai.ms_context_builder.ai_index.application.strategy;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.models.AIIndexRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoLineamiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LineamientoAIMapperStrategyTest {

    @InjectMocks
    private LineamientoAIMapperStrategy strategy;

    @Test
    @DisplayName("Debe mapear DocumentoLineamiento a AIIndexRequest con todos los campos poblados")
    void shouldMapDocumentoLineamientoToAIIndexRequestWhenFieldsArePopulated() {
        // Given
        DocumentoLineamiento doc = new DocumentoLineamiento();
        doc.setId("lin-001");
        doc.setTitulo("Lineamiento de Seguridad");
        doc.setContenido("Toda API debe requerir autenticación JWT.");
        doc.setTags(List.of("seguridad", "api", "jwt"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.id()).isEqualTo("lin-001");
        assertThat(result.tipo()).isEqualTo(TipoDocumento.LINEAMIENTO.name());
        assertThat(result.contenido()).contains("Toda API debe requerir autenticación JWT.");
        assertThat(result.tags()).containsExactly("seguridad", "api", "jwt");
    }

    @Test
    @DisplayName("Debe reemplazar contenido nulo por cadena vacía al mapear Lineamiento")
    void shouldHandleNullContenidoWhenMappingLineamiento() {
        // Given
        DocumentoLineamiento doc = new DocumentoLineamiento();
        doc.setId("lin-002");
        doc.setTitulo("Lineamiento sin contenido");
        doc.setContenido(null);
        doc.setTags(List.of("tag1"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).doesNotContain("null");
        assertThat(result.id()).isEqualTo("lin-002");
    }

    @Test
    @DisplayName("Debe retornar lista de tags vacía cuando los tags son nulos")
    void shouldHandleNullTagsListWhenMappingLineamiento() {
        // Given
        DocumentoLineamiento doc = new DocumentoLineamiento();
        doc.setId("lin-003");
        doc.setTitulo("Lineamiento sin tags");
        doc.setContenido("Contenido válido.");
        doc.setTags(null);

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("Debe asignar tipo LINEAMIENTO en el request al mapear DocumentoLineamiento")
    void shouldIncludeCorrectTipoInRequestWhenMappingLineamiento() {
        // Given
        DocumentoLineamiento doc = new DocumentoLineamiento();
        doc.setId("lin-004");
        doc.setTitulo("Lineamiento de Logging");
        doc.setContenido("Usar SLF4J como fachada de logging.");
        doc.setTags(List.of());

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.tipo()).isEqualTo(TipoDocumento.LINEAMIENTO.name());
        assertThat(result.metadata())
                .containsEntry("documentoId", "lin-004")
                .containsEntry("tipo", TipoDocumento.LINEAMIENTO.name())
                .containsEntry("titulo", "Lineamiento de Logging");
    }
}
