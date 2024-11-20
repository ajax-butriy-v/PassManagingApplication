plugins {
    `subproject-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.mongock.springboot.v3)
    implementation(libs.mongock.mongodb.springdata.v4.driver)
    implementation(project(":core"))
    implementation(project(":internal-api"))
    testImplementation(testFixtures(project(":pass-manager-svc")))
}
