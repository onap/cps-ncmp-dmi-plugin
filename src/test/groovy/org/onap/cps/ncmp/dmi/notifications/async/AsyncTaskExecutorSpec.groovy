package org.onap.cps.ncmp.dmi.notifications.async

import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

class AsyncTaskExecutorSpec extends Specification {

    def kafkaTemplate = Mock(KafkaTemplate)
    def dmiAsyncRequestResponseEventProducer = new DmiAsyncRequestResponseEventProducer(kafkaTemplate)
    def objectUnderTest = new AsyncTaskExecutor(dmiAsyncRequestResponseEventProducer)
    def dmiAsyncRequestResponseEventCreator =
        new DmiAsyncRequestResponseEventCreator();

    def 'Publish asynchronous message successfully.'() {
        given: 'an event'
            def event = dmiAsyncRequestResponseEventCreator.createEvent('{}', 'test_topic', '12345', 'OK', '200')
        when: 'the event published as asynchronous event'
            objectUnderTest.publishAsyncEvent('test_topic', '12345','{}', 'OK', '200')
        then: 'it will be published to the topic once'
            1 * kafkaTemplate.send(*_)
    }

    def 'Publish asynchronous failure message successfully.'() {
        given: 'an http client request exception'
            def exception = new HttpClientRequestException('some cm handle', 'Node not found', HttpStatus.INTERNAL_SERVER_ERROR)
        when: 'a failure event is published'
            objectUnderTest.publishAsyncFailureEvent('test_topic', '67890', exception)
        then: 'it will be published to the topic once'
            1 * kafkaTemplate.send(*_)
    }
}
