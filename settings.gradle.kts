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
include("pass-manager-svc:pass-owner")
include("pass-manager-svc:core")
include("pass-manager-svc:pass")
include("migration")
include("pass-manager-svc:migration")