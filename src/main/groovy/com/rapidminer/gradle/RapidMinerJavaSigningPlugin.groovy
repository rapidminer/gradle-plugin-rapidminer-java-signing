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
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.plugins.JavaPlugin



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

			ext {
				keystore = System.properties['keystore']
				storepass = System.properties['storepass']
				signAlias = System.properties['alias']
			}

			def isRelease = !version.endsWith('-SNAPSHOT')
			if(isRelease){
				project.logger.info "#### Release version detected. Signing release jars for version $version."
			}

			def signRequested =  project.hasProperty("signJar") && Boolean.parseBoolean(signJar)
			if(signRequested){
				project.logger.info "#### Signing of jars requested by 'signJar' property."
			}

			if(isRelease || signRequested){

				// ensure jar is rebuild in each build
				jar.outputs.upToDateWhen { false }

				// enhance jar task to sign the jar via Ant signjar task
				jar.doLast {
					if(!keystore){
						throw new GradleException("Cannot create release jar for ${project.name}. Missing keystore system property (e.g. -Dkeystore='/etc/Codesign.keystore'")
					} else if(!storepass){
						throw new GradleException("Cannot create release jar for ${project.name}. Missing storepass system property (e.g. -Dstorepass='grrrrrr'")
					}else if(!signAlias){
						throw new GradleException("Cannot create release jar for ${project.name}. Missing alias system property (e.g. -Dalias='rapidminer'")
					}
					ant.signjar(jar: jar.archivePath, alias: signAlias, keystore: keystore, storepass: storepass)
				}
			} else {
				project.logger.info "Version $version it not a release. Skipping signing of jar."
			}

		}
	}
}
