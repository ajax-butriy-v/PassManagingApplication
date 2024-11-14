plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "PassManagingApplication"
include("internal-api")
include("gateway")
include("pass-manager-svc")
include("core")
include("grpc-api")
include("common-proto")
include("pass-manager-svc:pass-type")
findProject(":pass-manager-svc:pass-type")?.name = "pass-type"
include("pass-manager-svc:pass-owner")
findProject(":pass-manager-svc:pass-owner")?.name = "pass-owner"
include("pass-manager-svc:core")
findProject(":pass-manager-svc:core")?.name = "core"
include("pass-manager-svc:pass")
findProject(":pass-manager-svc:pass")?.name = "pass"
