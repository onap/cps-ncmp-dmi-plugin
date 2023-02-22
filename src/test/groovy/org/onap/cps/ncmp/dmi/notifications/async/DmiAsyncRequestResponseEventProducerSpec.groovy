package org.onap.cps.ncmp.dmi.notifications.async

import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

class DmiAsyncRequestResponseEventProducerSpec extends Specification {

    def kafkaTemplate = Mock(KafkaTemplate)
    def objectUnderTest = new DmiAsyncRequestResponseEventProducer(kafkaTemplate)

    def 'Publish asynchronous event to target topic successfully.'() {
        given: 'an asynchronous event'
            def asynchEvent = Mock(DmiAsyncRequestResponseEvent)
            def requestId = UUID.randomUUID().toString()
        when: 'the event published to the topic'
            objectUnderTest.sendMessage(requestId, asynchEvent)
        then: 'the event is published by one time'
            1 * kafkaTemplate.send(*_)
    }

}
