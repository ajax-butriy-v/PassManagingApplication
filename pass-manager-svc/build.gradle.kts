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
    implementation("org.springframework.kafka:spring-kafka:3.2.4")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.23")

    implementation(project(":core"))
    implementation(project(":internal-api"))
    testImplementation(libs.nats.embedded)
    testImplementation("org.springframework.kafka:spring-kafka-test:3.2.4")
    testFixturesImplementation(libs.faker)
}
