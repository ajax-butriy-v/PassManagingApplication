plugins {
    `kotlin-conventions`
}

dependencies {
    implementation(project(":internal-api"))
    implementation(libs.spring.boot.starter.data.redis.reactive)
}
