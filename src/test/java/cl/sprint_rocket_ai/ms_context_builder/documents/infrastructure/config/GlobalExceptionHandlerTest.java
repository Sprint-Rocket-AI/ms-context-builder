package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.config;

import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Debe retornar 400 Bad Request cuando se lanza MethodArgumentNotValidException")
    void shouldReturnBadRequestWhenMethodArgumentNotValidExceptionIsThrown() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // When
        ResponseEntity<Map<String, Object>> result = handler.handleValidation(methodArgumentNotValidException);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).containsEntry("status", 400);
        assertThat(result.getBody()).containsEntry("error", "Bad Request");
    }

    @Test
    @DisplayName("Debe incluir la lista de errores de campo en el body cuando la validación falla")
    void shouldIncludeFieldErrorsInBodyWhenValidationFails() {
        // Given
        FieldError fieldError = new FieldError("documentoRequest", "titulo", "El título es obligatorio");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Map<String, Object>> result = handler.handleValidation(methodArgumentNotValidException);

        // Then
        assertThat(result.getBody()).containsKey("errores");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> errores = (List<Map<String, String>>) result.getBody().get("errores");
        assertThat(errores).hasSize(1);
        assertThat(errores.get(0))
                .containsEntry("campo", "titulo")
                .containsEntry("mensaje", "El título es obligatorio");
    }

    @Test
    @DisplayName("Debe usar 'valor inválido' como mensaje por defecto cuando el mensaje del error de campo es nulo")
    void shouldReturnDefaultMessageWhenFieldErrorMessageIsNull() {
        // Given
        FieldError fieldErrorSinMensaje = new FieldError("obj", "contenido", null);
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldErrorSinMensaje));

        // When
        ResponseEntity<Map<String, Object>> result = handler.handleValidation(methodArgumentNotValidException);

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, String>> errores = (List<Map<String, String>>) result.getBody().get("errores");
        assertThat(errores.get(0)).containsEntry("mensaje", "valor inválido");
    }

    @Test
    @DisplayName("Debe retornar 404 Not Found cuando se lanza EntityNotFoundException")
    void shouldReturnNotFoundWhenEntityNotFoundExceptionIsThrown() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("documento con id: abc-123");

        // When
        ResponseEntity<Map<String, Object>> result = handler.handleNotFound(ex);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).containsEntry("status", 404);
        assertThat(result.getBody()).containsEntry("error", "Not Found");
    }

    @Test
    @DisplayName("Debe incluir el mensaje de la excepción en el body cuando el recurso no es encontrado")
    void shouldIncludeExceptionMessageInBodyWhenEntityNotFound() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("documento con id: xyz-999");

        // When
        ResponseEntity<Map<String, Object>> result = handler.handleNotFound(ex);

        // Then
        assertThat(result.getBody()).containsKey("mensaje");
        assertThat(result.getBody().get("mensaje").toString()).contains("xyz-999");
    }

    @Test
    @DisplayName("Debe incluir el campo timestamp en el body de respuesta para todos los handlers")
    void shouldIncludeTimestampInResponseBodyForAllHandlers() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        EntityNotFoundException ex = new EntityNotFoundException("documento con id: ts-001");

        // When
        ResponseEntity<Map<String, Object>> validationResponse = handler.handleValidation(methodArgumentNotValidException);
        ResponseEntity<Map<String, Object>> notFoundResponse = handler.handleNotFound(ex);

        // Then
        assertThat(validationResponse.getBody()).containsKey("timestamp");
        assertThat(notFoundResponse.getBody()).containsKey("timestamp");
    }
}
