package cl.sprint_rocket_ai.ms_context_builder.ai_index.application.strategy;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.models.AIIndexRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NegocioAIMapperStrategyTest {

    @InjectMocks
    private NegocioAIMapperStrategy strategy;

    @Test
    @DisplayName("Debe mapear DocumentoNegocio a AIIndexRequest con todos los campos presentes")
    void shouldMapDocumentoNegocioToAIIndexRequestWhenAllFieldsPresent() {
        // Given
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId("neg-001");
        doc.setTitulo("Proceso de Alta de Cliente");
        doc.setContenido("El cliente debe completar el formulario de registro.");
        doc.setCriteriosAceptacion(List.of("El formulario valida RUT", "Se envía email de confirmación"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.id()).isEqualTo("neg-001");
        assertThat(result.tipo()).isEqualTo(TipoDocumento.NEGOCIO.name());
        assertThat(result.contenido()).contains("Proceso de Alta de Cliente");
        assertThat(result.contenido()).contains("El cliente debe completar el formulario de registro.");
        assertThat(result.tags()).containsExactly("El formulario valida RUT", "Se envía email de confirmación");
    }

    @Test
    @DisplayName("Debe omitir los bullets de criterios cuando la lista de criterios está vacía")
    void shouldHandleEmptyCriteriosAceptacionWhenMappingNegocio() {
        // Given
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId("neg-002");
        doc.setTitulo("Proceso sin criterios");
        doc.setContenido("Descripción del proceso.");
        doc.setCriteriosAceptacion(List.of());

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).contains("Criterios:");
        assertThat(result.contenido()).doesNotContain("- ");
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("Debe tratar criterios nulos como lista vacía al mapear DocumentoNegocio")
    void shouldHandleNullCriteriosAceptacionWhenMappingNegocio() {
        // Given
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId("neg-003");
        doc.setTitulo("Proceso con criterios nulos");
        doc.setContenido("Descripción.");
        doc.setCriteriosAceptacion(null);

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).doesNotContain("null");
        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("Debe formatear cada criterio con bullet point en el contenido cuando existen criterios")
    void shouldFormatCriteriosWithBulletPointsInContenidoWhenPresent() {
        // Given
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId("neg-004");
        doc.setTitulo("Proceso de Pago");
        doc.setContenido("El cliente paga en línea.");
        doc.setCriteriosAceptacion(List.of("Pago procesado en menos de 3s", "Se genera comprobante PDF"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.contenido()).contains("- Pago procesado en menos de 3s");
        assertThat(result.contenido()).contains("- Se genera comprobante PDF");
    }

    @Test
    @DisplayName("Debe usar los criterios de aceptación como tags en el request")
    void shouldIncludeCriteriosAsTagsInRequestWhenMappingNegocio() {
        // Given
        DocumentoNegocio doc = new DocumentoNegocio();
        doc.setId("neg-005");
        doc.setTitulo("Validación de Stock");
        doc.setContenido("Se valida stock antes de confirmar pedido.");
        doc.setCriteriosAceptacion(List.of("Stock suficiente", "Reserva creada en sistema"));

        // When
        AIIndexRequest result = strategy.map(doc);

        // Then
        assertThat(result.tags())
                .hasSize(2)
                .containsExactly("Stock suficiente", "Reserva creada en sistema");
    }
}
