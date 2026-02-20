plugins {
    `java-library`
}

group = "io.truelist"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.1")
}

tasks.test {
    useJUnitPlatform()
}
