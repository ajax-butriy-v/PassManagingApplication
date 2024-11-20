plugins {
    `kotlin-conventions`
}

dependencies {
    implementation(project(":internal-api"))
    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.mongodb.bson)
    implementation(libs.jackson.module.kotlin)
}
