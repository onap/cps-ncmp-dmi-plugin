/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications.async

import org.onap.cps.ncmp.dmi.exception.DmiException
import spock.lang.Specification

import java.util.concurrent.TimeoutException

class AsyncTaskExecutorSpec extends Specification {

    def objectUnderTest = new AsyncTaskExecutor(Mock(DmiAsyncRequestResponseEventProducerService))

    def 'Fail to publish message due to #scenario'() {
        when: 'a failure event is published'
            objectUnderTest.publishAsyncFailureEvent('topic-for-test', '12345', exceptionToThrow)
        then: 'a dmi exception is thrown'
            def exception = thrown(DmiException)
        and: 'the error message matches expected'
            assert exception.getMessage() == expectedExceptionMessage
        where: 'the following values are used'
            scenario       | exceptionToThrow                                        || expectedExceptionMessage
            'time out'     | new TimeoutException('408 Timed Out')                   || 'Request Timeout Error.'
            'server error' | new DmiException('500 Internal Server Error','details') || 'Internal Server Error.'
    }
}
