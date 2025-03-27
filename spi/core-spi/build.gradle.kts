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
    `java-test-fixtures`
}

dependencies {
    testFixturesApi(libs.edc.junit)
    testFixturesImplementation(libs.edc.junit)

    //TODO need to check why below dependency is not coming with edc.juinit
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testFixturesImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
    testFixturesImplementation("org.junit.platform:junit-platform-engine:1.10.0")
    testFixturesImplementation("org.assertj:assertj-core:3.27.3")
}

