plugins {
    `delta-coverage-conventions`
    `kotlin-conventions`
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

tasks.named("check") {
    dependsOn("deltaCoverage", "detektMain", "detektTest")
}
