package org.onap.cps.ncmp.dmi.notifications.avc

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig
import spock.lang.Specification

class KafkaConsumerFactorySpec extends Specification {

    def dmiKafkaConfig = Mock(DmiKafkaConfig)

    def objectUnderTest = new KafkaConsumerFactory(dmiKafkaConfig)

    def 'Create properties instance successfully'() {
        given: 'a simulated dmi kafka config'
            dmiKafkaConfig.getBootstrapServers() >> 'localhost:19092'
            dmiKafkaConfig.getKeyDeserializer() >> 'org.apache.kafka.common.serialization.StringDeserializer'
            dmiKafkaConfig.getValueDeserializer() >> 'org.springframework.kafka.support.serializer.JsonDeserializer'
            dmiKafkaConfig.getGroupId() >> 'ncmp-group'
        when: 'the get properties method is being called'
            def result = objectUnderTest.getKafkaProperties()
        then: 'a Properties instance is returned'
            assert  result instanceof Properties
        and: 'it has bootstrap server config "localhost:19092"'
            assert result.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG) == 'localhost:19092'
        and: 'it has bootstrap key deserializer "StringDeserializer"'
            assert result.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG) == 'org.apache.kafka.common.serialization.StringDeserializer'
    }

}
