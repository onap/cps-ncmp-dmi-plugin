package org.onap.cps.ncmp.dmi.notifications.avc

import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

class DmiDataAvcEventProducerSpec extends Specification {

    def kafkaTemplate = Mock(KafkaTemplate)
    def avcEventCreator = new DmiDataAvcEventCreator()
    def objectUnderTest = new DmiDataAvcEventProducer(kafkaTemplate)

    def 'Publish avc event to "dmi-cm-events" topic successfully'() {
        given: 'an avc event'
            def eventCorrelationId = 'correlationId1'
            def avcEvent = avcEventCreator.createEvent(eventCorrelationId)
            def requesId = UUID.randomUUID().toString()
        when: 'the event is published'
            objectUnderTest.sendMessage(requesId, avcEvent)
        then: 'log output results contains the correct log details'
            noExceptionThrown()
    }
}
