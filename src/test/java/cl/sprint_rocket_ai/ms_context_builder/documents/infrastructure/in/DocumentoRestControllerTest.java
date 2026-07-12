package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in;

import cl.sprint_rocket_ai.ms_context_builder.documents.application.DeleteDocumentoById;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.GetAllDocumentos;
import cl.sprint_rocket_ai.ms_context_builder.documents.application.GetDocumentoById;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoRestControllerTest {

    @Mock
    private GetAllDocumentos getAllDocumentos;

    @Mock
    private GetDocumentoById getDocumentoById;

    @Mock
    private DeleteDocumentoById deleteDocumentoById;

    @InjectMocks
    private DocumentoRestController controller;

    @Test
    @DisplayName("Debe retornar 200 OK con DocumentoResponse cuando getById encuentra el documento")
    void shouldReturnOkWithDocumentoResponseWhenGetByIdIsCalledWithValidId() {
        // Given
        String id = "doc-001";
        DocumentoResponse response = mock(DocumentoResponse.class);
        when(getDocumentoById.execute(id)).thenReturn(response);

        // When
        ResponseEntity<DocumentoResponse> result = controller.getById(id);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(getDocumentoById).execute(id);
    }

    @Test
    @DisplayName("Debe retornar 200 OK con la lista de documentos cuando getAll es invocado")
    void shouldReturnOkWithListWhenGetAllIsCalled() {
        // Given
        DocumentoResponse r1 = mock(DocumentoResponse.class);
        DocumentoResponse r2 = mock(DocumentoResponse.class);
        when(getAllDocumentos.execute()).thenReturn(List.of(r1, r2));

        // When
        ResponseEntity<List<DocumentoResponse>> result = controller.getAll();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2).containsExactly(r1, r2);
        verify(getAllDocumentos).execute();
    }

    @Test
    @DisplayName("Debe retornar 204 No Content cuando deleteById elimina el documento exitosamente")
    void shouldReturnNoContentWhenDeleteByIdIsCalledWithValidId() {
        // Given
        String id = "doc-001";
        doNothing().when(deleteDocumentoById).execute(id);

        // When
        ResponseEntity<DocumentoResponse> result = controller.deleteById(id);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(deleteDocumentoById).execute(id);
    }
}
