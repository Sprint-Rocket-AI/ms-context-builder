package cl.sprint_rocket_ai.ms_context_builder.ai_index.application.factory;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.strategy.*;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DocumentoAIMapperFactoryTest {

    @Mock
    private LineamientoAIMapperStrategy lineamientoStrategy;

    @Mock
    private SistemaAIMapperStrategy sistemaStrategy;

    @Mock
    private NegocioAIMapperStrategy negocioStrategy;

    @Mock
    private DDLAIMapperStrategy ddlStrategy;

    @InjectMocks
    private DocumentoAIMapperFactory factory;

    @Test
    @DisplayName("Debe retornar LineamientoAIMapperStrategy cuando el tipo es LINEAMIENTO")
    void shouldReturnLineamientoStrategyWhenTipoIsLINEAMIENTO() {
        // Given
        TipoDocumento tipo = TipoDocumento.LINEAMIENTO;

        // When
        AbstractDocumentoAIMapperStrategy result = factory.getMapperByDocumentType(tipo);

        // Then
        assertThat(result).isSameAs(lineamientoStrategy);
    }

    @Test
    @DisplayName("Debe retornar SistemaAIMapperStrategy cuando el tipo es SISTEMA")
    void shouldReturnSistemaStrategyWhenTipoIsSISTEMA() {
        // Given
        TipoDocumento tipo = TipoDocumento.SISTEMA;

        // When
        AbstractDocumentoAIMapperStrategy result = factory.getMapperByDocumentType(tipo);

        // Then
        assertThat(result).isSameAs(sistemaStrategy);
    }

    @Test
    @DisplayName("Debe retornar NegocioAIMapperStrategy cuando el tipo es NEGOCIO")
    void shouldReturnNegocioStrategyWhenTipoIsNEGOCIO() {
        // Given
        TipoDocumento tipo = TipoDocumento.NEGOCIO;

        // When
        AbstractDocumentoAIMapperStrategy result = factory.getMapperByDocumentType(tipo);

        // Then
        assertThat(result).isSameAs(negocioStrategy);
    }

    @Test
    @DisplayName("Debe retornar DDLAIMapperStrategy cuando el tipo es DDL")
    void shouldReturnDDLStrategyWhenTipoIsDDL() {
        // Given
        TipoDocumento tipo = TipoDocumento.DDL;

        // When
        AbstractDocumentoAIMapperStrategy result = factory.getMapperByDocumentType(tipo);

        // Then
        assertThat(result).isSameAs(ddlStrategy);
    }
}
