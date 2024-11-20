plugins {
    `subproject-conventions`
    `grpc-conventions`
}

dependencies {
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.mongock.springboot.v3)
    implementation(libs.mongock.mongodb.springdata.v4.driver)
    implementation(project(":pass-manager-svc:pass"))
    implementation(project(":pass-manager-svc:pass-owner"))
}
