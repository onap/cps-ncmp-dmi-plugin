package org.onap.cps.ncmp.dmi.notifications.avc

import spock.lang.Specification

class DmiDataAvcEventCreatorSpec extends Specification {

    def 'Create avc event successfully'() {
        given: 'a dmi data avc event creator'
            def objectUnderTest = new DmiDataAvcEventCreator()
        when: 'an avc event created'
            def eventCorrelationId = 'correlationId1'
            def result = objectUnderTest.createEvent(eventCorrelationId)
        then: 'avc event fields are as expected'
            assert result.getEventCorrelationId() == 'correlationId1'
            assert result.getEventType() == 'org.onap.cps.ncmp.event.model.AvcEvent'
            assert result.getEventSchema() == 'urn:cps:org.onap.cps.ncmp.event.model.AvcEvent'
            assert result.getEventSchemaVersion() == 'v1'
            assert result.getEventTarget() == 'NCMP'
            assert result.getEvent().getAdditionalProperties().get('payload') == 'Hello world!'
    }

}
