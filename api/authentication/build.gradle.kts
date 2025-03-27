/*
 * Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
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

plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

dependencies {
    implementation(project(":spi:core-spi"))
    implementation(libs.edc.spi.core)
    implementation(libs.edc.spi.web)
    implementation(libs.edc.spi.auth)
    implementation(libs.edc.spi.did)
    implementation(libs.edc.spi.token)
    implementation(libs.edc.spi.vc)

    implementation(libs.edc.lib.keys)
    implementation(libs.edc.vc.jwt) // JwtPresentationVerifie
    implementation(libs.edc.vc) // VerifiableCredentialValidationService
    implementation(libs.edc.identitytrust.service) //MultiFormatPresentationVerifier
    implementation(libs.edc.identitytrust.transform) //JwtToVerifiablePresentationTransformer
    implementation(libs.edc.lib.transform)
    implementation(libs.edc.lib.jsonld)
    implementation(libs.edc.lib.http)

    implementation(libs.nimbus.jwt)

    testImplementation(libs.restAssured)
    testImplementation(testFixtures(libs.edc.core.jersey))
}

edcBuild {
    swagger {
        apiGroup.set("directory-api")
    }
}

