package org.onap.cps.ncmp.dmi.config

import spock.lang.Specification

class DmiKafkaConfigSpec extends Specification {

    def objectUnderTest = DmiKafkaConfig.builder()
        .bootstrapServers('localhost:19092')
        .keySerializer('org.apache.kafka.common.serialization.StringSerializer')
        .valueSerializer('org.springframework.kafka.support.serializer.JsonSerializer')
        .build();

    def 'Dmi kafka config properties'() {
        expect: 'DMI config properties are set to values in builder object'
            objectUnderTest.getValueSerializer() == 'org.springframework.kafka.support.serializer.JsonSerializer'
            objectUnderTest.getBootstrapServers() == 'localhost:19092'
            objectUnderTest.getKeySerializer() == 'org.apache.kafka.common.serialization.StringSerializer'
            objectUnderTest.getGroupId() == null
    }

}
