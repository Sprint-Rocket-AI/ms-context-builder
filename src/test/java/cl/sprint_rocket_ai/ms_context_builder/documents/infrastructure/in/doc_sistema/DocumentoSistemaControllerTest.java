package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema;

import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_sistema.SaveDocumentoSistema;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_sistema.UpdateDocumentoSistema;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_sistema.dtos.DocumentoSistemaResponse;
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
class DocumentoSistemaControllerTest {

    @Mock
    private SaveDocumentoSistema saveDocumentoSistema;

    @Mock
    private UpdateDocumentoSistema updateDocumentoSistema;

    @InjectMocks
    private DocumentoSistemaController controller;

    private DocumentoSistemaRequest buildRequest() {
        return new DocumentoSistemaRequest("Servicio Pagos", "Microservicio de pagos.",
                List.of("https://github.com/org/pagos"), List.of("Java"), List.of("pagos"), List.of("dev1"));
    }

    private DocumentoSistemaResponse buildResponse(String id) {
        return new DocumentoSistemaResponse(id, "Servicio Pagos", "Microservicio de pagos.",
                TipoDocumento.SISTEMA, List.of("https://github.com/org/pagos"),
                List.of("Java"), List.of("dev1"), List.of("pagos"), null, null);
    }

    @Test
    @DisplayName("Debe retornar 201 Created con el documento sistema cuando create es invocado con request válido")
    void shouldReturnCreatedStatusWhenCreateIsCalledWithValidRequest() {
        // Given
        DocumentoSistemaRequest request = buildRequest();
        DocumentoSistemaResponse response = buildResponse("sis-001");
        when(saveDocumentoSistema.execute(request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoSistemaResponse> result = controller.create(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(saveDocumentoSistema).execute(request);
    }

    @Test
    @DisplayName("Debe retornar 200 OK con el documento actualizado cuando update es invocado con id válido")
    void shouldReturnOkWithUpdatedResponseWhenUpdateIsCalledWithValidId() {
        // Given
        String id = "sis-001";
        DocumentoSistemaRequest request = buildRequest();
        DocumentoSistemaResponse response = buildResponse(id);
        when(updateDocumentoSistema.execute(id, request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoSistemaResponse> result = controller.update(id, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(updateDocumentoSistema).execute(id, request);
    }
}
