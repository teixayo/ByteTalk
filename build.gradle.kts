plugins {
    id("java")
}

group = "com.bytetalk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

subprojects {
    apply(plugin = "java")

    group = "com.bytetalk"
    version = "1.0-SNAPSHOT"

    dependencies {
    }
}

tasks.test {
    useJUnitPlatform()
}