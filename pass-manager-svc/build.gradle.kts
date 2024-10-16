plugins {
    `spring-conventions`
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
    implementation(project(":internal-api"))
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.nats.embedded)
    testFixturesImplementation(libs.faker)
}
