/*
 *
 *   Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 *
 *   See the NOTICE file(s) distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Apache License, Version 2.0 which is available at
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 *
 */

plugins {
    id("application")
}

dependencies {
    runtimeOnly(project(":core:core-services"))
    runtimeOnly(project(":api:directory-api"))
    runtimeOnly(project(":api:management-api"))
    runtimeOnly(project(":api:authentication"))
    // will replace this with a mocked Did Resolver
    // runtimeOnly(libs.edc.identitydidweb)

    runtimeOnly(libs.edc.identitytrust.issuers)
    runtimeOnly(libs.bundles.bdrs.boot)
    runtimeOnly(libs.edc.core.did)
}

edcBuild {
    publish.set(false)
}
