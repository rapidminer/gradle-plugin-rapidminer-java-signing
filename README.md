## Introduction
By applying this plugin the Jars created for release versions get signed automatically.
When building releases the System properties 'keystore', 'alias' and 'storepass' need to be specified.

## How to use (requires Gradle 2.4+)
	plugins {
		id 'com.rapidminer.java-signing' version <plugin version>
	}
	
## Applied Plugins
* java

## Added Tasks
_Adds no tasks_
