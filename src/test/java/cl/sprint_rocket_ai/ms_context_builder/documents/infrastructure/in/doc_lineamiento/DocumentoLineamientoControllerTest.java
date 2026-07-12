package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento;

import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_lineamiento.SaveDocumentoLineamiento;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_lineamiento.UpdateDocumentoLineamiento;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_lineamiento.dtos.DocumentoLineamientoResponse;
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
class DocumentoLineamientoControllerTest {

    @Mock
    private SaveDocumentoLineamiento saveDocumentoLineamiento;

    @Mock
    private UpdateDocumentoLineamiento updateDocumentoLineamiento;

    @InjectMocks
    private DocumentoLineamientoController controller;

    private DocumentoLineamientoRequest buildRequest() {
        return new DocumentoLineamientoRequest("Lineamiento API", "Usar JWT.", List.of("seguridad"));
    }

    private DocumentoLineamientoResponse buildResponse(String id) {
        return new DocumentoLineamientoResponse(id, "Lineamiento API", "Usar JWT.",
                TipoDocumento.LINEAMIENTO, List.of("seguridad"), null, null);
    }

    @Test
    @DisplayName("Debe retornar 201 Created con el lineamiento cuando create es invocado con request válido")
    void shouldReturnCreatedStatusWhenCreateIsCalledWithValidRequest() {
        // Given
        DocumentoLineamientoRequest request = buildRequest();
        DocumentoLineamientoResponse response = buildResponse("lin-001");
        when(saveDocumentoLineamiento.execute(request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoLineamientoResponse> result = controller.create(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(saveDocumentoLineamiento).execute(request);
    }

    @Test
    @DisplayName("Debe retornar 200 OK con el lineamiento actualizado cuando update es invocado con id válido")
    void shouldReturnOkWithUpdatedResponseWhenUpdateIsCalledWithValidId() {
        // Given
        String id = "lin-001";
        DocumentoLineamientoRequest request = buildRequest();
        DocumentoLineamientoResponse response = buildResponse(id);
        when(updateDocumentoLineamiento.execute(id, request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoLineamientoResponse> result = controller.update(id, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(updateDocumentoLineamiento).execute(id, request);
    }
}
