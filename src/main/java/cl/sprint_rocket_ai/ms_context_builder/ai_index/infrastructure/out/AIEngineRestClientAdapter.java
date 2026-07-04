package cl.sprint_rocket_ai.ms_context_builder.ai_index.infrastructure.out;

import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.exceptions.AIEngineIntegrationException;
import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.models.AIIndexRequest;
import cl.sprint_rocket_ai.ms_context_builder.ai_index.domain.ports.out.AIIndexPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adapter de salida que envía {@link AIIndexRequest} al servicio externo
 * de indexación AI usando {@link RestClient}.
 */
@Component
public final class AIEngineRestClientAdapter implements AIIndexPortOut {

    private static final Logger log = LoggerFactory.getLogger(AIEngineRestClientAdapter.class);
    private static final String INDEX_PATH = "/rag/index";
    private static final String INDEX_BY_ID_PATH = "/rag/index/{id}";

    private final RestClient restClient;

    public AIEngineRestClientAdapter(@Qualifier("aiEngineRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void index(AIIndexRequest request) {
        log.info("Enviando documento {} (tipo={}) al indexador AI", request.id(), request.tipo());
        try {
            restClient.post()
                    .uri(INDEX_PATH)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Documento {} indexado correctamente", request.id());
        } catch (RestClientResponseException ex) {
            log.error("Error HTTP {} al indexar documento {}: {}",
                    ex.getStatusCode(), request.id(), ex.getResponseBodyAsString(), ex);
            throw new AIEngineIntegrationException("Error al indexar documento en ai-engine con id: " + request.id(), ex);
        } catch (Exception ex) {
            log.error("Error inesperado al indexar documento {} en ai-engine", request.id(), ex);
            throw new AIEngineIntegrationException("Error inesperado al indexar documento en ai-engine con id: " + request.id(), ex);
        }
    }

    @Override
    public void update(String id, AIIndexRequest request) {
        log.info("Actualizando índice AI del documento {} (tipo={})", id, request.tipo());
        try {
            restClient.put()
                    .uri(INDEX_BY_ID_PATH, id)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Índice AI del documento {} actualizado correctamente", id);
        } catch (RestClientResponseException ex) {
            log.error("Error HTTP {} al actualizar índice del documento {}: {}",
                    ex.getStatusCode(), id, ex.getResponseBodyAsString(), ex);
            throw new AIEngineIntegrationException("Error al actualizar índice en ai-engine para documento con id: " + id, ex);
        } catch (Exception ex) {
            log.error("Error inesperado al actualizar índice del documento {} en ai-engine", id, ex);
            throw new AIEngineIntegrationException("Error inesperado al actualizar índice en ai-engine para documento con id: " + id, ex);
        }
    }

    @Override
    public void deleteById(String id) {
        log.info("Eliminando índice AI del documento {}", id);
        try {
            restClient.delete()
                    .uri(INDEX_BY_ID_PATH, id)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Índice AI del documento {} eliminado correctamente", id);
        } catch (RestClientResponseException ex) {
            log.error("Error HTTP {} al eliminar índice del documento {}: {}",
                    ex.getStatusCode(), id, ex.getResponseBodyAsString(), ex);
            throw new AIEngineIntegrationException("Error al eliminar índice en ai-engine para documento con id: " + id, ex);
        } catch (Exception ex) {
            log.error("Error inesperado al eliminar índice del documento {} en ai-engine", id, ex);
            throw new AIEngineIntegrationException("Error inesperado al eliminar índice en ai-engine para documento con id: " + id, ex);
        }
    }
}
