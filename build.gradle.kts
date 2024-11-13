plugins {
    `delta-coverage-conventions`
    `kotlin-conventions`
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            setUrl("https://packages.confluent.io/maven/")
        }
        maven {
            url = uri(extra["repository"].toString())
            credentials(AwsCredentials::class.java) {
                accessKey = extra["AWS_ACCESS_KEY_ID"].toString()
                secretKey = extra["AWS_SECRET_ACCESS_KEY"].toString()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("deltaCoverage", "detektMain", "detektTest")
}
