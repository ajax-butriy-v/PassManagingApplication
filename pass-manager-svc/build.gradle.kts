plugins {
    `spring-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.mongodb.bson)
    implementation(project(":core"))
    implementation(project(":internal-api"))
    implementation(project(":pass-manager-svc:migration"))
    implementation(project(":pass-manager-svc:pass"))
    implementation(project(":pass-manager-svc:pass-type"))
    implementation(project(":pass-manager-svc:pass-owner"))
    testFixturesImplementation(libs.faker)

}
