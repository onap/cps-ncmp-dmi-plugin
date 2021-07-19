package org.onap.cps.ncmp.dmi.service.client

import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class SdncRestconfClientSpec extends Specification {

    def mockSdncProperties = Mock(DmiConfiguration.SdncProperties)
    def mockRestTemplate = Mock(RestTemplate)
    def objectUnderTest = new SdncRestconfClient(mockSdncProperties, mockRestTemplate)

    def 'test getop for sdnc.'() {
        given: 'geturl and mediatype'
            def geturl = '/testurl'
            def mediatype = 'application/json'

        and: 'configuration data'
            mockSdncProperties.baseUrl >> 'http://test-sdnc-uri'
            mockSdncProperties.authUsername >> 'test-username'
            mockSdncProperties.authPassword >> 'test-password'
            mockSdncProperties.topologyId >> 'testTopologyId'

        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)

        when: 'getOp is invoked'
            def result = objectUnderTest.getOperation(geturl, mediatype)

        then: 'the rest template is called with the correct uri and json in the body'
            1 * mockRestTemplate.exchange({ it.toString() == 'http://test-sdnc-uri/testurl' }, HttpMethod.GET, _ as HttpEntity, String.class) >> mockResponseEntity

        and: 'the output of the method is the same as the output from the test template'
             result == mockResponseEntity
    }
}