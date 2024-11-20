plugins {
    `subproject-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(project(":core"))
    implementation(project(":internal-api"))
    testImplementation(testFixtures(project(":pass-manager-svc")))
}
