package org.onap.cps.ncmp.dmi.notifications.avc

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponse
import org.onap.cps.ncmp.event.model.SubscriptionEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration

@SpringBootTest(classes = [SubscriptionEventConsumer])
@Testcontainers
@DirtiesContext
class SubscriptionEventConsumerSpec extends MessagingBaseSpec {

    def kafkaConsumer = new KafkaConsumer<>(consumerConfigProperties('ncmp-group'))

    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'

    @SpringBean
    SubscriptionEventConsumer objectUnderTest = new SubscriptionEventConsumer(kafkaTemplate)

    def 'Sends subscription event response successfully.'() {
        given: 'an subscription event response'
            def value = new SubscriptionEventResponse(clientId: 'client-1',
                status: 'accepted')
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        and: 'consumer has a subscription'
            kafkaConsumer.subscribe([testTopic] as List<String>)
        when: 'an event is published'
            def key = 'key-1'
            objectUnderTest.sendSubscriptionResponseMessage(key, value)
        and: 'topic is polled'
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'poll returns one record'
            assert records.size() == 1
        and: 'record key matches the expected event key'
            def record = records.iterator().next()
            assert key == record.key
        and: 'record value matches the expected event value'
            def expectedValue = objectMapper.writeValueAsString(value)
            assert expectedValue == record.value
    }

    def 'Consume valid message'() {
        given: 'an event'
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def testEventSent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        when: 'the valid event is consumed'
            objectUnderTest.consumeSubscriptionEvent(testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
    }
}