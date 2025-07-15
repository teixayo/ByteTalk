plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}


tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("ByteTalk")
    manifest {
        attributes["Main-Class"] = "me.teixayo.bytetalk.Main"
    }

    doLast {
        println("✅ Fat JAR created at: ${archiveFile.get().asFile.absolutePath}")
    }
}
dependencies {

    implementation(libs.netty.codec)
    implementation(libs.netty.codec.http)
    implementation(libs.netty.handler)
    implementation(libs.netty.transport.native.epoll)
    implementation(variantOf(libs.netty.transport.native.epoll) { classifier("linux-x86_64") })
    implementation(variantOf(libs.netty.transport.native.epoll) { classifier("linux-aarch_64") })
    implementation(libs.netty.transport.native.iouring)
    implementation(variantOf(libs.netty.transport.native.iouring) { classifier("linux-x86_64") })
    implementation(variantOf(libs.netty.transport.native.iouring) { classifier("linux-aarch_64") })
    implementation(libs.netty.transport.native.kqueue)
    implementation(variantOf(libs.netty.transport.native.kqueue) { classifier("osx-x86_64") })
    implementation(variantOf(libs.netty.transport.native.kqueue) { classifier("osx-aarch_64") })
    implementation(libs.mongodb)
    implementation(libs.elasticsearch)
    implementation(libs.jedis)
    implementation(libs.json)
    implementation(libs.slf4j)
    implementation(libs.bundles.log4j)
    implementation(libs.jegl)
    implementation(libs.jwt)
    implementation(libs.snakeyaml)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.junit)
    testImplementation(libs.bundles.testcontainer)
    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
}

tasks.test {
    useJUnitPlatform()
}
