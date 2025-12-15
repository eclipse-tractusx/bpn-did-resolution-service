/*
 * Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 * Copyright (c) 2025 Cofinity-X GmbH
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

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin

plugins {
    checkstyle
    `java-library`
    `maven-publish`
    jacoco
    `jacoco-report-aggregation`
    alias(libs.plugins.edc.build)
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.docker)
}

val edcVersion = libs.versions.edc

buildscript {
    dependencies {
        val edcVersion: String = libs.versions.edc.asProvider().get()

        classpath("org.eclipse.edc.autodoc:org.eclipse.edc.autodoc.gradle.plugin:$edcVersion")
    }
}

// include all subprojects in the jacoco report aggregation
project.subprojects.forEach {
    dependencies {
        jacocoAggregation(project(it.path))
    }
}

val edcBuildId = libs.plugins.edc.build.get().pluginId

allprojects {

    apply(plugin = edcBuildId)
    apply(plugin = "org.eclipse.edc.autodoc")
    apply(plugin = "jacoco")

    // configure which version of the annotation processor to use. defaults to the same version as the plugin
    configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
        outputDirectory.set(project.layout.buildDirectory.asFile)
        processorVersion.set(edcVersion.asProvider())
    }

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        swagger {
            title.set((project.findProperty("apiTitle") ?: "Tractus-X BDRS Server REST API") as String)
            description =
                (project.findProperty("apiDescription")
                    ?: "Tractus-X REST APIs - merged by OpenApiMerger") as String
            outputFilename.set(project.name)
            outputDirectory.set(file("${rootProject.projectDir.path}/resources/openapi/yaml"))
            resourcePackages = setOf("org.eclipse.tractusx.bdrs")
        }

    }
}

// the "dockerize" task is added to all projects that use the `shadowJar` plugin, e.g. runtimes
subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.github.johnrengelman.shadow") &&
            file("${project.projectDir}/src/main/docker/Dockerfile").exists()
        ) {
            val shadowJarTask = tasks.named("shadowJar").get()

            // this task copies some legal docs into the build folder, so we can easily copy them into the docker images
            val copyLegalDocs = tasks.register("copyLegalDocs", Copy::class) {

                into("${project.layout.buildDirectory.asFile.get()}")
                into("legal") {
                    from("${project.rootProject.projectDir}/SECURITY.md")
                    from("${project.rootProject.projectDir}/NOTICE.md")
                    from("${project.rootProject.projectDir}/DEPENDENCIES")
                    from("${project.rootProject.projectDir}/LICENSE")
                    from("${projectDir}/notice.md")

                }
                mustRunAfter(shadowJarTask)
                mustRunAfter(tasks.named(JavaPlugin.JAR_TASK_NAME))
            }

            //actually apply the plugin to the (sub-)project
            apply(plugin = "com.bmuschko.docker-remote-api")
            // configure the "dockerize" task
            tasks.register("dockerize", DockerBuildImage::class) {                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
                images.add("${project.name}:${project.version}")
                images.add("${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null)
                    platform.set(System.getProperty("platform"))
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                buildArgs.put("ADDITIONAL_FILES", "build/legal/*")
                inputDir.set(file(dockerContextDir))

                dependsOn(shadowJarTask)
                dependsOn(copyLegalDocs)
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
