plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "org.example.Main"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Runs the Euchre web snapshot server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.example.GameServer"
}
