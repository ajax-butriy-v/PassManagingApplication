plugins {
    `spring-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.mongock.springboot.v3)
    implementation(libs.mongock.mongodb.springdata.v4.driver)
    implementation(libs.nats)
    implementation(libs.spring.kafka)
    implementation(libs.reactor.kafka)
    implementation(project(":core"))
    implementation(project(":internal-api"))
    testFixturesImplementation(libs.faker)
}
