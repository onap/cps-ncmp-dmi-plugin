package org.onap.cps.ncmp.dmi.config.kafka

import io.cloudevents.CloudEvent
import io.cloudevents.kafka.CloudEventDeserializer
import io.cloudevents.kafka.CloudEventSerializer
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = [KafkaProperties, KafkaConfig])
@EnableSharedInjection
@EnableConfigurationProperties
class KafkaConfigSpec extends Specification {

    @Shared
    @Autowired
    KafkaTemplate<String, String> legacyEventKafkaTemplate

    @Shared
    @Autowired
    KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate

    def 'Verify kafka template serializer and deserializer configuration for #eventType.'() {
        expect: 'kafka template is instantiated'
            assert kafkaTemplateInstance.properties['beanName'] == beanName
        and: 'verify event key and value serializer'
            assert kafkaTemplateInstance.properties['producerFactory'].configs['value.serializer'].asType(String.class).contains(valueSerializer.getCanonicalName())
        and: 'verify event key and value deserializer'
            assert kafkaTemplateInstance.properties['consumerFactory'].configs['spring.deserializer.value.delegate.class'].asType(String.class).contains(delegateDeserializer.getCanonicalName())
        where: 'the following event type is used'
            eventType      | kafkaTemplateInstance    || beanName                   | valueSerializer      | delegateDeserializer
            'legacy event' | legacyEventKafkaTemplate || 'legacyEventKafkaTemplate' | JsonSerializer       | JsonDeserializer
            'cloud event'  | cloudEventKafkaTemplate  || 'cloudEventKafkaTemplate'  | CloudEventSerializer | CloudEventDeserializer
    }
}
