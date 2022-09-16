import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    
    id("net.mamoe.mirai-console") version "2.12.2"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("io.netty:netty-all:4.1.67.Final")
    implementation("com.github.Sunshine-wzy:rkon-core:1.2.2")
    implementation("com.github.Sunshine-wzy:SunnyFlow:1.0.4")
    
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8"))
    
    shadowLink("nl.vv32.rcon:rcon")
    shadowLink("com.github.Sunshine-wzy:rkon-core")
    shadowLink("com.github.Sunshine-wzy:SunnyFlow")


    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

group = "io.github.sunshinewzy"
version = "1.2.6"

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenCentral()
    maven("https://jitpack.io")
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
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
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