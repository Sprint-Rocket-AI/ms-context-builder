package cl.sprint_rocket_ai.ms_context_builder.documents.application;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.application.AIIndexService;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.exceptions.EntityNotFoundException;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoContextoMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDocumentoByIdTest {

    @Mock
    private DocumentoContextoMongoRepository repository;

    @Mock
    private AIIndexService aiIndexService;

    @InjectMocks
    private DeleteDocumentoById deleteDocumentoById;

    @Test
    @DisplayName("Debe eliminar el índice AI y el documento cuando el id existe")
    void shouldDeleteDocumentoAndCallAiIndexWhenIdExists() {
        // Given
        String id = "doc-001";
        when(repository.existsById(id)).thenReturn(true);

        // When
        deleteDocumentoById.execute(id);

        // Then
        verify(repository).existsById(id);
        verify(aiIndexService).deleteById(id);
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException cuando el id no existe")
    void shouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
        // Given
        String id = "id-inexistente";
        when(repository.existsById(id)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> deleteDocumentoById.execute(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id);

        verify(repository).existsById(id);
    }

    @Test
    @DisplayName("No debe llamar a deleteById del repositorio cuando el id no existe")
    void shouldNotCallRepositoryDeleteWhenIdNotFound() {
        // Given
        String id = "id-inexistente";
        when(repository.existsById(id)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> deleteDocumentoById.execute(id))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).deleteById(any());
        verify(aiIndexService, never()).deleteById(any());
    }
}
