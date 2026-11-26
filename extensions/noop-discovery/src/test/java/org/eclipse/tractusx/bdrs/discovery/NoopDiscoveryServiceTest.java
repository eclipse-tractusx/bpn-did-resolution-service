/*
 * Copyright (c) 2026 Cofinity-X GmbH
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.tractusx.bdrs.discovery;

import org.eclipse.edc.protocol.spi.discovery.DiscoveryRequest;
import org.eclipse.edc.protocol.spi.discovery.DiscoveryUrlResolver;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class NoopDiscoveryServiceTest {

    private final NoopDiscoveryService service = new NoopDiscoveryService();

    @Test
    void discover_alwaysFails() {
        // DiscoveryRequest is a final record and cannot be mocked on every JDK, so a real instance is used
        var request = new DiscoveryRequest("did:web:counter-party", "https://counter-party.example.com");

        var result = service.discover("did:web:counter-party", request);

        assertThat(result.failed()).isTrue();
        assertThat(result.reason()).isEqualTo(ServiceFailure.Reason.UNEXPECTED);
        assertThat(result.getFailureDetail()).contains("not supported");
    }

    @Test
    void registerResolver_isIgnored() {
        var resolver = mock(DiscoveryUrlResolver.class);

        service.registerResolver(resolver);

        verifyNoInteractions(resolver);
    }
}
