plugins {
    `subproject-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.kafka)
    implementation(libs.nats)
    implementation(project(":core"))
    implementation(project(":pass-manager-svc:pass-owner"))
    implementation(project(":pass-manager-svc:pass-type"))
    implementation(project(":internal-api"))
    testImplementation(libs.natsTest)
    testImplementation(libs.kafkaTest)
    testImplementation(testFixtures(project(":pass-manager-svc")))
}
