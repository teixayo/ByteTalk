plugins {
    `java-library`
}

group = "com.bytetalk"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

subprojects {
    apply(plugin = "java-library")

    group = "com.bytetalk"
    version = "1.0-SNAPSHOT"

    dependencies {
    }
}

tasks.test {
    useJUnitPlatform()
}
