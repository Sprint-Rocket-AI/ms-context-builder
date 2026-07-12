package cl.sprint_rocket_ai.ms_context_builder.documents.application;

import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoContexto;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoContextoMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDocumentoByIdTest {

    @Mock
    private DocumentoContextoMongoRepository repository;

    @InjectMocks
    private GetDocumentoById getDocumentoById;

    @Test
    @DisplayName("Debe retornar DocumentoResponse cuando el id existe en el repositorio")
    void shouldReturnDocumentoResponseWhenIdExists() {
        // Given
        String id = "doc-001";
        DocumentoContexto documento = mock(DocumentoContexto.class);
        DocumentoResponse response = mock(DocumentoResponse.class);

        when(repository.findById(id)).thenReturn(Optional.of(documento));
        when(documento.toResponse()).thenReturn(response);

        // When
        DocumentoResponse result = getDocumentoById.execute(id);

        // Then
        assertThat(result).isEqualTo(response);
        verify(repository).findById(id);
        verify(documento).toResponse();
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException cuando el id no existe en el repositorio")
    void shouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
        // Given
        String id = "id-inexistente";
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> getDocumentoById.execute(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id);

        verify(repository).findById(id);
    }
}
