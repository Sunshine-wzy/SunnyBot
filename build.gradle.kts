import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.6.5"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("io.netty:netty-all:4.1.67.Final")
    
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8"))
}

group = "io.github.sunshinewzy"
version = "1.1.6"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
    jcenter()
    
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven { 
        url = uri("http://maven.imagej.net/content/groups/public")
        name = "imagej.public"
    }
}

tasks {
    jar {
        archiveBaseName.set("SunnyBot")
        archiveVersion.set(project.version.toString())
//        destinationDirectory.set(file("F:/Kotlin/Debug/mirai/plugins"))
    }

    compileKotlin {
        kotlinOptions {
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}