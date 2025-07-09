plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("me.teixayo.bytetalk.Main")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}


tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "me.teixayo.bytetalk.Main"
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
