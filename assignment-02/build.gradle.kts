plugins {
    id("java")
    id("application")
}

group = "it.unibo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/io.vertx/vertx-core
    implementation("io.vertx:vertx-core:5.0.10")
    // Source: https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
}

application {
    mainClass.set("it.unibo.reactive.TestReactiveGUI")
}

tasks.test {
    useJUnitPlatform()
}