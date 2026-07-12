package cl.sprint_rocket_ai.ms_context_builder.documents.application;

import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoContexto;
import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoResponse;
import cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.persistences.mongodb.DocumentoContextoMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllDocumentosTest {

    @Mock
    private DocumentoContextoMongoRepository repository;

    @InjectMocks
    private GetAllDocumentos getAllDocumentos;

    @Test
    @DisplayName("Debe retornar la lista de respuestas cuando el repositorio contiene documentos")
    void shouldReturnAllDocumentosWhenRepositoryHasData() {
        // Given
        DocumentoContexto doc1 = mock(DocumentoContexto.class);
        DocumentoContexto doc2 = mock(DocumentoContexto.class);
        DocumentoResponse response1 = mock(DocumentoResponse.class);
        DocumentoResponse response2 = mock(DocumentoResponse.class);

        when(repository.findAll()).thenReturn(List.of(doc1, doc2));
        when(doc1.toResponse()).thenReturn(response1);
        when(doc2.toResponse()).thenReturn(response2);

        // When
        List<DocumentoResponse> result = getAllDocumentos.execute();

        // Then
        assertThat(result).hasSize(2).containsExactly(response1, response2);
        verify(repository).findAll();
        verify(doc1).toResponse();
        verify(doc2).toResponse();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el repositorio no tiene documentos")
    void shouldReturnEmptyListWhenRepositoryIsEmpty() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<DocumentoResponse> result = getAllDocumentos.execute();

        // Then
        assertThat(result).isEmpty();
        verify(repository).findAll();
    }
}
