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

    def 'test getOperation for sdnc.'() {

        given: 'geturl and mediatype'
            def getResourceUrl = '/gerResourceUrl'

        and: 'sdnc properties'
            mockSdncProperties.baseUrl >> 'http://test-sdnc-uri'
            mockSdncProperties.authUsername >> 'test-username'
            mockSdncProperties.authPassword >> 'test-password'
            mockSdncProperties.topologyId >> 'testTopologyId'

        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)

            mockRestTemplate.getForEntity({ it.toString() == 'http://test-sdnc-uri/gerResourceUrl' }, String.class, _ as HttpEntity) >> mockResponseEntity


        when: 'getOperation is invoked'
            def result = objectUnderTest.getOperation(getResourceUrl)

        then: 'the output of the method is the same as the output from the test template'
            result == mockResponseEntity
    }
}