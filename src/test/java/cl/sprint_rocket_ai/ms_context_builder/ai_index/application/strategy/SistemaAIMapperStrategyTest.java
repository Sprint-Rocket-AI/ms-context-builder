package cl.sprint_rocket_ai.ms_context_builder.ai_index.application.strategy;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.models.AIIndexRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoSistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SistemaAIMapperStrategyTest {

    @InjectMocks
    private SistemaAIMapperStrategy strategy;

    @Test
    @DisplayName("Debe mapear DocumentoSistema a AIIndexRequest con todos los campos presentes")
    void shouldMapDocumentoSistemaToAIIndexRequestWhenAllFieldsPresent() {
        // Given
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId("sis-001");
        doc.setTitulo("Servicio de Pagos");
        doc.setContenido("Microservicio que gestiona transacciones.");
        doc.setStack(List.of("Java", "Spring Boot", "Kafka"));
        doc.setUrlRepos(List.of("https://github.com/org/pagos"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.id()).isEqualTo("sis-001");
        assertThat(result.tipo()).isEqualTo(TipoDocumento.SISTEMA.name());
        assertThat(result.contenido()).contains("Servicio de Pagos");
        assertThat(result.contenido()).contains("Microservicio que gestiona transacciones.");
        assertThat(result.tags()).containsExactly("Java", "Spring Boot", "Kafka");
    }

    @Test
    @DisplayName("Debe tratar stack nulo como lista vacía al mapear DocumentoSistema")
    void shouldHandleNullStackWhenMappingSistema() {
        // Given
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId("sis-002");
        doc.setTitulo("Sistema sin stack");
        doc.setContenido("Descripción.");
        doc.setStack(null);
        doc.setUrlRepos(List.of("https://github.com/org/repo"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.tags()).isEmpty();
        assertThat(result.contenido()).doesNotContain("null");
    }

    @Test
    @DisplayName("Debe tratar urlRepos nulo como lista vacía en la metadata al mapear DocumentoSistema")
    void shouldHandleNullUrlReposWhenMappingSistema() {
        // Given
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId("sis-003");
        doc.setTitulo("Sistema sin repos");
        doc.setContenido("Descripción.");
        doc.setStack(List.of("Python"));
        doc.setUrlRepos(null);

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.metadata()).containsKey("repositorios");
        @SuppressWarnings("unchecked")
        List<String> repos = (List<String>) result.metadata().get("repositorios");
        assertThat(repos).isEmpty();
    }

    @Test
    @DisplayName("Debe incluir la lista de repositorios en la metadata cuando urlRepos está presente")
    void shouldIncludeRepositoriosInMetadataWhenUrlReposPresent() {
        // Given
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId("sis-004");
        doc.setTitulo("Sistema de Inventario");
        doc.setContenido("Gestiona el inventario.");
        doc.setStack(List.of("Node.js"));
        doc.setUrlRepos(List.of("https://github.com/org/inventario", "https://github.com/org/inventario-ui"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.metadata()).containsKey("repositorios");
        @SuppressWarnings("unchecked")
        List<String> repos = (List<String>) result.metadata().get("repositorios");
        assertThat(repos).containsExactly(
                "https://github.com/org/inventario",
                "https://github.com/org/inventario-ui"
        );
    }

    @Test
    @DisplayName("Debe unir los elementos del stack con coma en el contenido al mapear DocumentoSistema")
    void shouldJoinStackWithCommaInContenidoWhenMappingSistema() {
        // Given
        DocumentoSistema doc = new DocumentoSistema();
        doc.setId("sis-005");
        doc.setTitulo("API Gateway");
        doc.setContenido("Punto de entrada unificado.");
        doc.setStack(List.of("Spring Cloud Gateway", "Redis", "Prometheus"));
        doc.setUrlRepos(List.of());

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).contains("Spring Cloud Gateway, Redis, Prometheus");
    }
}
