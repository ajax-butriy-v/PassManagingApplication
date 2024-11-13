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
    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.mongock.springboot.v3)
    implementation(libs.mongock.mongodb.springdata.v4.driver)
    implementation(libs.kafka)
    implementation(libs.nats)
    implementation(project(":core"))
    implementation(project(":internal-api"))
    testImplementation(libs.natsTest)
    testImplementation(libs.kafkaTest)
    testFixturesImplementation(libs.faker)
}
