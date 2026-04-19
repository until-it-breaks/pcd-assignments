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
    // Source: https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
}

tasks.test {
    useJUnitPlatform()
}