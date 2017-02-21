/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.context;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerResponseFilter;

/** Servlet filter for the registry API that addresses CORS issues.
 * We could have done this as a {@link ContainerResponseFilter},
 * but doing it as a servlet {@link Filter} means it can be
 * applied more generally (e.g., to /swagger.json).
 *
 * Based on the Swagger sample ApiOriginFilter class here:
 * https://github.com/swagger-api/swagger-samples/blob/master/java/java-jersey2/src/main/java/io/swagger/sample/util/ApiOriginFilter.java
 *  */
public class ApiOriginFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /** Filter that adds this header: <pre>Access-Control-Allow-Origin: *</pre>
     * to responses.
     */
    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        HttpServletResponse hsr = (HttpServletResponse) response;
        hsr.addHeader("Access-Control-Allow-Origin", "*");
        // Future work, if needed: add additional headers to
        // control methods and other headers:
//        hsr.addHeader("Access-Control-Allow-Methods",
//                      "GET, POST, DELETE, PUT");
//        hsr.addHeader("Access-Control-Allow-Headers",
//                      "Content-Type, api_key, Authorization");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
