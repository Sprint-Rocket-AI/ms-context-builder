package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in;

import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;


@Tag(
		name = "Documentos",
		description = "API para la vizualización de los documentos"
)
public interface DocumentoController {

	@Operation(
			summary = "Busca documento por id"
	)
	@ApiResponse(
			responseCode = "200",
			description = "Documento encontrado",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DocumentoResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Solicitud inválida")
	ResponseEntity<DocumentoResponse> getById(
			@Parameter(description = "ID del documento") String id
	);

	@Operation(
			summary = "Busca todos los documentos"
	)
	@ApiResponse(
			responseCode = "200",
			description = "Documentos encontrados",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DocumentoResponse.class))
	)
	ResponseEntity<List<DocumentoResponse>> getAll();

	@Operation(
			summary = "Busca todos los documentos"
	)
	@ApiResponse(
			responseCode = "204",
			description = "Elimina documento por id",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DocumentoResponse.class))
	)
	ResponseEntity<DocumentoResponse> deleteById(
			@Parameter(description = "ID del documento") String id
	);

}
