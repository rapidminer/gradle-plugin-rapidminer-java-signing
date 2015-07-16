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
                ext.keystore = System.properties['keystore']
            } else {
                project.logger.info "Java Signing: 'keystore' property already set. Skipping setting of 'keystore' external property."
            }


            if (!project.hasProperty('storepass')) {
                ext.storepass = System.properties['storepass']
            } else {
                project.logger.info "Java Signing: 'storepass' property already set. Skipping setting of 'storepass' external property."
            }

            if (!project.hasProperty('alias')) {
                ext.alias = System.properties['alias']
            } else {
                project.logger.info "Java Signing: 'alias' property already set. Skipping setting of 'alias' external property."
            }

            def signRequested = project.hasProperty('signJar') && Boolean.parseBoolean(signJar)
            if (signRequested) {
                project.logger.info "Java Signing: Signing of jars requested by 'signJar' property."
            } else {
                project.logger.info "Java Signing: Signing of jars was not requested by 'signJar' property."
            }

            def isRelease = !version.endsWith('-SNAPSHOT')
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
                        throw new GradleException("Cannot create release jar for ${project.name}. Missing keystore system property (e.g. -Dkeystore='/etc/Codesign.keystore'")
                    } else if (!storepass) {
                        throw new GradleException("Cannot create release jar for ${project.name}. Missing storepass system property (e.g. -Dstorepass='grrrrrr'")
                    } else if (!alias) {
                        throw new GradleException("Cannot create release jar for ${project.name}. Missing alias system property (e.g. -Dalias='rapidminer'")
                    }
                    ant.signjar(jar: jar.archivePath, alias: alias, keystore: keystore, storepass: storepass)
                }


                afterEvaluate {

                    try {
                        // check if task exists
                        project.tasks.getByName('shadowJar')

                        // ensure shadowJar is rebuild in each build
                        shadowJar.outputs.upToDateWhen { false }

                        shadowJar.doLast {
                            project.logger.info "Java Signing: Signing shadowJar for ${project.name}."
                            if (!keystore) {
                                throw new GradleException("Cannot create release shadowJar for ${project.name}. Missing keystore system property (e.g. -Dkeystore='/etc/Codesign.keystore'")
                            } else if (!storepass) {
                                throw new GradleException("Cannot create release shadowJar for ${project.name}. Missing storepass system property (e.g. -Dstorepass='grrrrrr'")
                            } else if (!alias) {
                                throw new GradleException("Cannot create release shadowJar for ${project.name}. Missing alias system property (e.g. -Dalias='rapidminer'")
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
