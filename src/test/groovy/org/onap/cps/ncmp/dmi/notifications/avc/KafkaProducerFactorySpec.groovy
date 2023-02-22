package org.onap.cps.ncmp.dmi.notifications.avc

import org.apache.kafka.clients.producer.ProducerConfig
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig
import spock.lang.Specification

class KafkaProducerFactorySpec extends Specification {

    def dmiKafkaConfig = Mock(DmiKafkaConfig)

    def objectUnderTest = new KafkaProducerFactory(dmiKafkaConfig)

    def 'Create properties instance successfully'() {
        given: 'a simulated dmi kafka config'
            dmiKafkaConfig.getBootstrapServers() >> 'localhost:19092'
            dmiKafkaConfig.getKeySerializer() >> 'org.apache.kafka.common.serialization.StringSerializer'
            dmiKafkaConfig.getValueSerializer() >> 'org.springframework.kafka.support.serializer.JsonSerializer'
        when: 'the get properties method is being called'
            def result = objectUnderTest.getKafkaProperties()
        then: 'a Properties instance is returned'
            assert  result instanceof Properties
        and: 'it has bootstrap server config "localhost:19092"'
            assert result.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) == 'localhost:19092'
        and: 'it has bootstrap value serializer "JsonSerializer"'
            assert result.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) == 'org.springframework.kafka.support.serializer.JsonSerializer'
    }

}
