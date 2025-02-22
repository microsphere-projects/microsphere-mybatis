plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/spring-plugin")
    }
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}