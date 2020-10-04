import org.gradle.kotlin.dsl.support.compileKotlinScriptModuleTo

plugins {
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "1.0-RC-dev-28"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.2")
}

group = "io.github.sunshinewzy"
version = "1.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}