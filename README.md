## Introduction
By applying this plugin the Jars created for release versions get signed automatically.
When building releases the configuration can be done either by project variables (keystore, alias, and storepass) or 
via the System environment variables 'com.rapidminer.java-signing.keystore', 'com.rapidminer.java-signing.alias' and 'com.rapidminer.java-signing.storepass'. 

## How to use (requires Gradle 2.4+)
	plugins {
		id 'com.rapidminer.java-signing' version <plugin version>
	}
	
## Applied Plugins
* java

## Added Tasks
_Adds no tasks_
