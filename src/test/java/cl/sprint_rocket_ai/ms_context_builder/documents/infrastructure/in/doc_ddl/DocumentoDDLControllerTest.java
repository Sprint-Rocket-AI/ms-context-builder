package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl;

import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_ddl.SaveDocumentoDDL;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.doc_ddl.UpdateDocumentoDDL;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.MotorBaseDatos;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.enums.TipoDocumento;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.ColumnaRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.DocumentoDDLRequest;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.DocumentoDDLResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_ddl.dtos.TablaRequest;
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
class DocumentoDDLControllerTest {

    @Mock
    private SaveDocumentoDDL saveDocumentoDDL;

    @Mock
    private UpdateDocumentoDDL updateDocumentoDDL;

    @InjectMocks
    private DocumentoDDLController controller;

    private DocumentoDDLRequest buildRequest() {
        ColumnaRequest columna = new ColumnaRequest("id", "BIGINT", true, false, false, true, null, "PK");
        TablaRequest tabla = new TablaRequest("usuarios", "public", "desc", List.of(columna), List.of());
        return new DocumentoDDLRequest("Schema", "CREATE TABLE ...", MotorBaseDatos.POSTGRESQL, "1.0", List.of(tabla));
    }

    private DocumentoDDLResponse buildResponse(String id) {
        return new DocumentoDDLResponse(id, "Schema", "CREATE TABLE ...", TipoDocumento.DDL,
                MotorBaseDatos.POSTGRESQL, "1.0", List.of(), null, null);
    }

    @Test
    @DisplayName("Debe retornar 201 Created con el documento DDL cuando create es invocado con request válido")
    void shouldReturnCreatedStatusWhenCreateIsCalledWithValidRequest() {
        // Given
        DocumentoDDLRequest request = buildRequest();
        DocumentoDDLResponse response = buildResponse("ddl-001");
        when(saveDocumentoDDL.execute(request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoDDLResponse> result = controller.create(request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
        verify(saveDocumentoDDL).execute(request);
    }

    @Test
    @DisplayName("Debe retornar 200 OK con el documento actualizado cuando update es invocado con id válido")
    void shouldReturnOkWithUpdatedResponseWhenUpdateIsCalledWithValidId() {
        // Given
        String id = "ddl-001";
        DocumentoDDLRequest request = buildRequest();
        DocumentoDDLResponse response = buildResponse(id);
        when(updateDocumentoDDL.execute(id, request)).thenReturn(response);

        // When
        ResponseEntity<DocumentoDDLResponse> result = controller.update(id, request);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(updateDocumentoDDL).execute(id, request);
    }
}
