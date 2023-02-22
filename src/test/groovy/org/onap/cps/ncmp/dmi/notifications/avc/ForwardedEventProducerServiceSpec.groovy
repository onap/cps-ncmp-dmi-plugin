package org.onap.cps.ncmp.dmi.notifications.avc


import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.ConfigException
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig
import spock.lang.Specification

import java.util.concurrent.TimeoutException

class ForwardedEventProducerServiceSpec extends Specification {

    def 'Create producer factory with invalid config cause an exception'() {
        given: 'an invalid config'
            def invalidKafkaConfig = DmiKafkaConfig.builder()
                .bootstrapServers('localhost:19092')
                .keySerializer('org.apache.kafka.common.serialization.Invalid')
                .valueSerializer('org.springframework.kafka.support.serializer.Invalid')
                .build();
            def invalidFactory = new KafkaProducerFactory(invalidKafkaConfig)
        when: 'the try to get an instance of factory'
            invalidFactory.getForwardedEventKafkaProducer()
        then: 'an config exception exception is thrown'
            thrown(ConfigException)
    }

    def 'Publish event takes longer than expected'() {
        given: 'a producer'
            def mockProducer = Mock(KafkaProducer)
            def objectUnderTest = new ForwardedEventProducerService(mockProducer)
        and: 'a producer record'
            def mockRecord = Mock(ProducerRecord)
        and: 'producer throws time out exception'
            mockProducer.send(mockRecord) >> { throw new TimeoutException('Unable to publish due to timeout') }
        when: 'the record is published'
            objectUnderTest.publish(mockRecord)
        then: 'an time out exception has been catch'
            noExceptionThrown()
    }
}
