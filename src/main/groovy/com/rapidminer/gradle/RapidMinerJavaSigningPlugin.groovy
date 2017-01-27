/*
 * Copyright 2013-2014 RapidMiner GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rapidminer.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

/**
 * This plugin adds Jar signing capabilities to a Java project.
 *
 * @author Nils Woehler
 *
 */
class RapidMinerJavaSigningPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.configure(project) {
            apply plugin: 'java'

            if (!project.hasProperty("keystore")) {
                ext.keystore = System.getenv('com.rapidminer.java-signing.keystore')
                project.logger.info "Java Signing: Retrieve 'keystore' property from system environment (key: com.rapidminer.java-signing.keystore, value: ${keystore})."
            } else {
                project.logger.info "Java Signing: 'keystore' property already (value: ${keystore}) set by project property. Skipping lookup of 'keystore' lookup property."
            }


            if (!project.hasProperty('storepass')) {
                ext.storepass = System.getenv('com.rapidminer.java-signing.storepass')
                project.logger.info "Java Signing: Retrieve 'storepass' property from system environment (key: com.rapidminer.java-signing.storepass)."
            } else {
                project.logger.info "Java Signing: 'storepass' property already set by project property. Skipping lookup of 'storepass' lookup property."
            }

            if (!project.hasProperty('alias')) {
                ext.alias = System.getenv('com.rapidminer.java-signing.alias')
                project.logger.info "Java Signing: Retrieve 'alias' property from system environment (key: com.rapidminer.java-signing.alias, value: ${alias})."
            } else {
                project.logger.info "Java Signing: 'alias' property already set by project property. Skipping lookup of 'alias' system property."
            }

            if (!project.hasProperty('signJar')) {
                ext.signJar = System.getenv('com.rapidminer.java-signing.signJar')
                project.logger.info "Java Signing: Retrieve 'signJar' property from system environment (key: com.rapidminer.java-signing.signJar, value: ${signJar})."
            } else {
                project.logger.info "Java Signing: 'signJar' property already set by project property. Skipping lookup of 'signJar' system property."
            }

            def signRequested = signJar && Boolean.parseBoolean(signJar)
            if (signRequested) {
                project.logger.info "Java Signing: Signing of jars requested by 'signJar' property."
            } else {
                project.logger.info "Java Signing: Signing of jars was not requested by 'signJar' property."
            }

            def isRelease = !version.endsWith('-SNAPSHOT') && !version.endsWith('-BETA')
            if (!signRequested) {
                if (isRelease) {
                    project.logger.info "Java Signing: Release version detected. Signing release jars for version $version."
                } else {
                    project.logger.info "Java Signing: No release version detected. Skipping signing jars for version $version."
                }
            }

            if (isRelease || signRequested) {

                // ensure jar is rebuild in each build
                jar.outputs.upToDateWhen { false }

                // enhance jar task to sign the jar via Ant signjar task
                jar.doLast {
                    project.logger.info "Java Signing: Signing jar for ${project.name}."
                    if (!keystore) {
                        throw new GradleException("Cannot create signed jar for ${project.name}. Missing keystore property.")
                    } else if (!storepass) {
                        throw new GradleException("Cannot create signed jar for ${project.name}. Missing storepass property.")
                    } else if (!alias) {
                        throw new GradleException("Cannot create signed jar for ${project.name}. Missing alias property.")
                    }
                    ant.signjar(jar: jar.archivePath, alias: alias, keystore: keystore, storepass: storepass)
                }


                afterEvaluate {

                    try {
                        // check if testJar task exists
                        project.tasks.getByName('testJar')

                        // ensure shadowJar is rebuild in each build
                        testJar.outputs.upToDateWhen { false }

                        testJar.doLast {
                            project.logger.info "Java Signing: Signing testJar for ${project.name}."
                            if (!keystore) {
                                throw new GradleException("Cannot create signed testJar for ${project.name}. Missing keystore property.")
                            } else if (!storepass) {
                                throw new GradleException("Cannot create signed testJar for ${project.name}. Missing storepass property.")
                            } else if (!alias) {
                                throw new GradleException("Cannot create signed testJar for ${project.name}. Missing alias property.")
                            }
                            ant.signjar(jar: testJar.archivePath, alias: alias, keystore: keystore, storepass: storepass)
                        }

                        // check if shadowJar task exists
                        project.tasks.getByName('shadowJar')

                        // ensure shadowJar is rebuild in each build
                        shadowJar.outputs.upToDateWhen { false }

                        shadowJar.doLast {
                            project.logger.info "Java Signing: Signing shadowJar for ${project.name}."
                            if (!keystore) {
                                throw new GradleException("Cannot create signed shadowJar for ${project.name}. Missing keystore property.")
                            } else if (!storepass) {
                                throw new GradleException("Cannot create signed shadowJar for ${project.name}. Missing storepass property.")
                            } else if (!alias) {
                                throw new GradleException("Cannot create signed shadowJar for ${project.name}. Missing alias property.")
                            }
                            ant.signjar(jar: shadowJar.archivePath, alias: alias, keystore: keystore, storepass: storepass)
                        }
                    } catch (UnknownTaskException e) {
                        project.logger.debug('Cannot configure shadowJar task signing. Project does not apply shadow plugin.')
                    }
                }
            } else {
                project.logger.info "Java Signing: Skipping signing of jar for ${project.name}"
            }

        }
    }
}
