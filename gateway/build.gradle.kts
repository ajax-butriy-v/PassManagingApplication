plugins {
    `spring-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.nats)
    implementation(libs.grpc.spring.boot.starter)
    implementation(libs.mongodb.bson)
    implementation(libs.grpc.server.spring.boot.starter)
    implementation(project(":internal-api"))
    implementation(project(":core"))
    implementation(project(":grpc-api"))
}
