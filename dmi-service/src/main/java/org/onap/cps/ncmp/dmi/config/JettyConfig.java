package org.onap.cps.ncmp.dmi.config;

import java.util.EnumSet;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

    /**
     * Customizes the Jetty server factory to allow encoded slashes in URI paths.
     *
     * @param jettyServletWebServerFactory the Jetty servlet web server factory to customize
     */
    @Override
    public void customize(final JettyServletWebServerFactory jettyServletWebServerFactory) {
        jettyServletWebServerFactory.addServerCustomizers(server -> {
            for (final Connector connector : server.getConnectors()) {
                for (final ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
                    if (connectionFactory instanceof HttpConnectionFactory) {
                        final HttpConfiguration httpConfiguration
                                = ((HttpConnectionFactory) connectionFactory).getHttpConfiguration();
                        httpConfiguration.setUriCompliance(UriCompliance.from(EnumSet.of(
                                UriCompliance.Violation.AMBIGUOUS_PATH_SEPARATOR)));
                    }
                }
            }
        });
    }
}
