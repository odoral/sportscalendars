plugins {
    id("java")
    id("io.ktor.plugin") version "2.3.11"
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
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.htmlunit:htmlunit:4.2.0")
    implementation("org.mnode.ical4j:ical4j:3.2.18")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("com.google.guava:guava:33.2.1-jre")
    runtimeOnly("ch.qos.logback:logback-core:1.5.6")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}