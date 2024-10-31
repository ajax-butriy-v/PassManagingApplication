import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.protobuf)
    `kotlin-conventions`
    `grpc-conventions`
}

dependencies {
    api(libs.protobuf)
    api(project(":common-proto"))
}

protobuf {
    protoc {
        // TODO version
        artifact = libs.protoc.get().toString()
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.59.0"
        }
        id("reactor-grpc") {
            artifact = "com.salesforce.servicelibs:reactor-grpc:1.2.4"
        }
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
                create("reactor-grpc")
            }
        }
    }
}
