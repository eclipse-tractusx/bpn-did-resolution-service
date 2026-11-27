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
import org.eclipse.edc.protocol.spi.discovery.DiscoveryResponse;
import org.eclipse.edc.protocol.spi.discovery.DiscoveryService;
import org.eclipse.edc.protocol.spi.discovery.DiscoveryUrlResolver;
import org.eclipse.edc.spi.result.ServiceResult;

import java.util.List;

/**
 * {@link DiscoveryService} that does not support protocol discovery.
 * <p>
 * BDRS is not a dataspace connector and never discovers counter-party DSP endpoints. This implementation only exists
 * to satisfy the injection point of the EDC DCP core extension (see {@link NoopDiscoveryServiceExtension}). Registered resolvers are ignored and every discovery
 * attempt fails explicitly.
 */
public class NoopDiscoveryService implements DiscoveryService {

    @Override
    public ServiceResult<List<DiscoveryResponse>> discover(String counterPartyId, DiscoveryRequest request) {
        return ServiceResult.unexpected("Protocol discovery is not supported by BDRS");
    }

    @Override
    public void registerResolver(DiscoveryUrlResolver resolver) {
        // no-op: BDRS does not perform protocol discovery
    }
}
