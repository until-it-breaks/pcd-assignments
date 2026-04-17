plugins {
    id("java")
}

group = "it.unibo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/io.vertx/vertx-core
    implementation("io.vertx:vertx-core:5.0.10")
}

tasks.test {
    useJUnitPlatform()
}