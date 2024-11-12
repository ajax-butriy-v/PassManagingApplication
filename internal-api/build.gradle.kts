plugins {
    alias(libs.plugins.protobuf)
    `kotlin-conventions`
}

dependencies {
    api(libs.protobuf)
    api(project(":common-proto"))
}
protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
}
