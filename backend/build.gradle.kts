plugins {
    id("application")
}

group = "com.bytetalk"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass.set("me.teixayo.bytetalk.Main") // ← Replace with your actual main class
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
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
