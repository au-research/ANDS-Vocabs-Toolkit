/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.context;

import io.swagger.annotations.Contact;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/** Empty interface that serves as as an anchor to which the
 * top-level Swagger definition is attached.
 */
@SwaggerDefinition(
        info = @Info(
                description = "Public access to the Vocabulary Registry API",
                // The @Info annotation requires that a version be specified,
                // but its value is not used. See
                // https://github.com/swagger-api/swagger-core/issues/1594
                // for the defect. Instead, the value is defined in
                // the SwaggerBootstrapper class.
                version = "see SwaggerBootstrapper for version",
                // The @Info annotation requires that a title be specified,
                // but its value is not used. See
                // https://github.com/swagger-api/swagger-core/issues/1594
                // for the defect. Instead, the value is defined in web.xml
                // as the init-param "swagger.api.title".
                title = "Vocabulary Registry API",
                termsOfService = "http://documentation.ands.org.au/",
                contact = @Contact(
                   name = "ANDS Services",
                   email = "services@ands.org.au",
                   url = "http://ands.org.au/"
                ),
                license = @License(
                   name = "Apache 2.0",
                   url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        consumes = { "application/xml", "application/json" },
        produces = { "application/xml", "application/json" },
        schemes = {SwaggerDefinition.Scheme.HTTP,
                SwaggerDefinition.Scheme.HTTPS},
        tags = {
                @Tag(name = "Default",
                     description = "Publicly-accessible methods"),
                @Tag(name = "Private",
                     description = "Tag used to denote operations as private")
        },
        externalDocs = @ExternalDocs(
                value = "Research Vocabularies Australia",
                url = "http://documentation.ands.org.au/")
)
public interface SwaggerInterface {
}
