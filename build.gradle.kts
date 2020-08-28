plugins {
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(group = "io.vertx", name = "vertx-core", version = "3.9.2")
    testImplementation(group = "io.vertx", name = "vertx-rx-java2", version = "3.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
