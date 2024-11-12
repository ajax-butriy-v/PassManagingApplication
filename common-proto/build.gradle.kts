plugins {
    alias(libs.plugins.protobuf)
    `kotlin-conventions`
}

dependencies {
    api(libs.protobuf)
}
protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
}
