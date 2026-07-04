package cl.sprint_rocket_ai.ms_context_builder.documents.infrastructure.in.doc_negocio.dtos;

import cl.sprint_rocket_ai.ms_context_builder.documents.domain.models.DocumentoNegocio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(
        description = "DTO para la creación/actualización de un Documento de Negocio.",
        example = """
                {
                  "titulo": "Especificación de Requisitos de Usuario",
                  "contenido": "El sistema debe permitir a los usuarios registrarse con su correo electrónico.",
                  "tags": ["onboarding", "registro"],
                  "criteriosAceptacion": [
                    "El usuario debe poder registrarse con email",
                    "Se debe validar el correo"
                  ]
                }
                """
)
public record DocumentoNegocioRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        @Schema(description = "Título del documento.", example = "Especificación de Requisitos de Usuario")
        String titulo,

        @NotBlank(message = "El contenido es obligatorio")
        @Schema(description = "Contenido del documento en formato Markdown o texto plano.",
                example = "El sistema debe permitir a los usuarios registrarse con su correo electrónico.")
        String contenido,


        @Schema(description = "Etiquetas asociadas al documento.", example = "[\"pagos\",\"onboarding\"]")
        List<String> tags,

        @Schema(description = "Criterios de aceptación.",
                example = "[\"El usuario debe poder registrarse con email\",\"Se debe validar el correo\"]")
        List<String> criteriosAceptacion

) {
    public void applyTo(DocumentoNegocio target) {
        target.setTitulo(titulo);
        target.setContenido(contenido);
        target.setTags(tags);
        target.setCriteriosAceptacion(criteriosAceptacion);
    }
}
