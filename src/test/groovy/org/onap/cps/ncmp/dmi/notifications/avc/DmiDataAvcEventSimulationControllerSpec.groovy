package org.onap.cps.ncmp.dmi.notifications.avc


import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(DmiDataAvcEventSimulationController.class)
@WithMockUser
class DmiDataAvcEventSimulationControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    def kafkaTemplate = Mock(KafkaTemplate)

    @SpringBean
    DmiDataAvcEventProducer dmiDataAvcEventProducer = new DmiDataAvcEventProducer(kafkaTemplate)

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'Simulates event successfully.'() {
        given: 'an URL for the controller'
            def getModuleUrl = "$basePathV1/simulateDmiDataEvent?numberOfSimulatedEvents=3"
        when: 'the request is got called'
            def response = mvc.perform(get(getModuleUrl)
                .contentType(MediaType.APPLICATION_JSON).content())
                .andReturn().response
        then: 'the avc event is published by three times'
            3 * kafkaTemplate.send(*_)
        and: 'response status is success'
            response.status == OK.value()
    }
}
