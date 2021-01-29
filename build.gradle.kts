plugins {
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "1.0-RC-dev-28"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("io.netty:netty-all:4.1.48.Final")
    
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

group = "io.github.sunshinewzy"
version = "1.0.8"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven { 
        url = uri("http://maven.imagej.net/content/groups/public")
        name = "imagej.public"
    }
}