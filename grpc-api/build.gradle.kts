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
        artifact = libs.protoc.get().toString()
    }
    plugins {
        id("grpc") {
            artifact = libs.grpc.asProvider().get().toString()
        }
        id("reactor-grpc") {
            artifact = libs.grpc.reactor.get().toString()
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
