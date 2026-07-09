/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2025-2026 OpenInfra Foundation Europe. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.config

import org.eclipse.jetty.server.ConnectionFactory
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory
import spock.lang.Specification

class JettyConfigSpec extends Specification {

    def objectUnderTest = new JettyConfig()
    def server = Mock(Server)
    def connector = Mock(Connector)
    def mockServerCustomizer

    def setup() {
        server.getConnectors() >> [connector]
        def jettyServletWebServerFactory = new JettyServletWebServerFactory()
        objectUnderTest.customize(jettyServletWebServerFactory)
        mockServerCustomizer = jettyServletWebServerFactory.serverCustomizers.first()
    }

    def 'Customize with Http connection factory.'() {
        given: 'a mocked HttpConnectionFactory with HttpConfiguration'
            def connectionFactory = Mock(HttpConnectionFactory)
            def httpConfig = Mock(HttpConfiguration)
            connectionFactory.getHttpConfiguration() >> httpConfig
            connector.getConnectionFactories() >> [connectionFactory]
        when: 'the server customizer is applied to the mocked Jetty server'
            mockServerCustomizer.customize(server)
        then: 'the HTTP configuration is updated to allow ambiguous path separators'
            1 * httpConfig.setUriCompliance({
                it.toString().contains('AMBIGUOUS_PATH_SEPARATOR')
            })
    }

    def 'Customize with other connection factory.'() {
        given: 'a mocked non-Http ConnectionFactory'
            def connectionFactory = Mock(ConnectionFactory)
            connector.getConnectionFactories() >> [connectionFactory]
        when: 'the server customizer is applied to the mocked Jetty server'
            mockServerCustomizer.customize(server)
        then: 'no configuration methods are called on the connection factory'
            0 * connectionFactory._
    }
}
