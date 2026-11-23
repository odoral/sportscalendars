plugins {
    id("java")
    id("io.ktor.plugin") version "2.3.13"
}

application {
    mainClass.set("org.doral.sportcalendars.webscraper.runner.WebScraperApp")
}

group = "org.doral"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.7")
    implementation("org.htmlunit:htmlunit:4.21.0")
    implementation("org.mnode.ical4j:ical4j:3.2.19")
    implementation("org.slf4j:slf4j-api:2.0.20")
    implementation("com.google.guava:guava:33.8.0-jre")
    runtimeOnly("ch.qos.logback:logback-core:1.5.38")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.38")

    constraints {
        implementation("commons-beanutils:commons-beanutils:1.11.0") {
            because("Fixes CVE-2025-48734 in the transitive commons-validator dependency")
        }
    }

    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:3.12.4")
}

tasks.test {
    useJUnitPlatform()
}