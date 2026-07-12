package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio;

import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_negocio.SaveDocumentoNegocio;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_negocio.UpdateDocumentoNegocio;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos.DocumentoNegocioResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoNegocioControllerTest {

    @Mock
    private SaveDocumentoNegocio saveDocumentoNegocio;

    @Mock
    private UpdateDocumentoNegocio updateDocumentoNegocio;

    @InjectMocks
    private DocumentoNegocioController controller;

    private DocumentoNegocioRequest buildRequest() {
        return new DocumentoNegocioRequest("Alta Cliente", "Proceso de registro.",
                List.of("onboarding"), List.of("Se valida RUT"));
    }

    private DocumentoNegocioResponse buildResponse(String id) {
        return new DocumentoNegocioResponse(id, "Alta Cliente", "Proceso de registro.",
                TipoDocumento.NEGOCIO, List.of("onboarding"), List.of("Se valida RUT"), null, null);
    }

    @Test
    @DisplayName("Debe retornar 201 Created con el documento de negocio cuando create es invocado con request válido")
    void shouldReturnCreatedStatusWhenCreateIsCalledWithValidRequest() {
        // Given
        DocumentoNegocioRequest request = buildRequest();
        DocumentoNegocioResponse response = buildResponse("neg-001");
        when(saveDocumentoNegocio.execute(request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoNegocioResponse> result = controller.create(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(saveDocumentoNegocio).execute(request);
    }

    @Test
    @DisplayName("Debe retornar 200 OK con el documento actualizado cuando update es invocado con id válido")
    void shouldReturnOkWithUpdatedResponseWhenUpdateIsCalledWithValidId() {
        // Given
        String id = "neg-001";
        DocumentoNegocioRequest request = buildRequest();
        DocumentoNegocioResponse response = buildResponse(id);
        when(updateDocumentoNegocio.execute(id, request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoNegocioResponse> result = controller.update(id, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(updateDocumentoNegocio).execute(id, request);
    }
}
