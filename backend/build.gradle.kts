plugins {
    id("java")
}

group = "com.bytetalk"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21


repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("org.yaml:snakeyaml:2.2")
    implementation("io.netty:netty-transport-native-epoll:4.2.1.Final")
    implementation("io.netty:netty-transport-native-epoll:4.2.1.Final:linux-x86_64")
    implementation("io.netty:netty-transport-native-epoll:4.2.1.Final:linux-aarch_64")

    implementation("io.netty:netty-transport-native-kqueue:4.2.1.Final")
    implementation("io.netty:netty-transport-native-kqueue:4.2.1.Final:osx-x86_64")
    implementation("io.netty:netty-transport-native-kqueue:4.2.1.Final:osx-aarch_64")

    implementation("io.netty:netty-transport-native-io_uring:4.2.1.Final")
    implementation("io.netty:netty-transport-native-io_uring:4.2.1.Final:linux-x86_64")
    implementation("io.netty:netty-transport-native-io_uring:4.2.1.Final:linux-aarch_64")

    implementation("io.netty:netty-handler:4.2.1.Final")
    implementation("io.netty:netty-codec-http:4.2.1.Final")
    implementation("io.netty:netty-codec:4.2.1.Final")

    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")

    implementation("org.mongodb:mongodb-driver-sync:4.11.0")
    implementation("co.elastic.clients:elasticsearch-java:8.17.0")

    implementation("org.json:json:20240303")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("com.github.teixayo:JEGL:v1.0")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("redis.clients:jedis:5.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

    testImplementation("org.testcontainers:testcontainers:1.21.0")
    testImplementation("org.testcontainers:junit-jupiter:1.21.0")
    testImplementation("org.testcontainers:mongodb:1.21.0")
}

tasks.test {
    useJUnitPlatform()
}
