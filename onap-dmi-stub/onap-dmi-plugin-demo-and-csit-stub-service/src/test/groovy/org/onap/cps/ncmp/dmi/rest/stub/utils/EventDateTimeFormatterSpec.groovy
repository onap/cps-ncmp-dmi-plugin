package org.onap.cps.ncmp.dmi.rest.stub.utils;

import spock.lang.Specification

import java.time.Year

class EventDateTimeFormatterSpec extends Specification {

    def 'Get ISO formatted date and time.' () {
        expect: 'iso formatted date and time starts with current year'
            assert EventDateTimeFormatter.getCurrentIsoFormattedDateTime().startsWith(String.valueOf(Year.now()))
    }

    def 'Convert date time from string to OffsetDateTime type.'() {
        when: 'date time as a string is converted to OffsetDateTime type'
            def result = EventDateTimeFormatter.toIsoOffsetDateTime('2024-05-28T18:28:02.869+0100')
        then: 'the result convert back back to a string is the same as the original timestamp (except the format of timezone offset)'
            assert result.toString() == '2024-05-28T18:28:02.869+01:00'
    }

    def 'Convert blank string.' () {
        expect: 'converting a blank string result in null'
            assert EventDateTimeFormatter.toIsoOffsetDateTime(' ') == null
    }

}

