plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation ("org.seleniumhq.selenium:selenium-devtools-v120:4.16.1")
    implementation ("org.seleniumhq.selenium:selenium-support:4.16.1")
    implementation ("org.seleniumhq.selenium:selenium-chrome-driver:4.16.1")
    implementation ("com.itextpdf:itext7-core:7.1.9")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}