plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "PassManagingApplication"
include("internal-api")
include("gateway")
include("pass-manager-svc")
