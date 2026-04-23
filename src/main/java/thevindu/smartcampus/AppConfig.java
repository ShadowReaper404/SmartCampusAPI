/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration.
 * @ApplicationPath here is optional when using web.xml servlet mapping,
 * but kept for clarity.
 */
@ApplicationPath("/api/v1")
public class AppConfig extends Application {
}
