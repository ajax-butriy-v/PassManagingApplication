package com.example.gateway.arch

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.onionArchitecture
import kotlin.test.Test

internal class GatewayOnionArchitectureTest {
    @Test
    fun `module should be following valid onion architecture`() {
        val importedClasses: JavaClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .withImportOption(ImportOption.DoNotIncludeGradleTestFixtures())
            .importPackages("com.example.gateway")

        val rule = onionArchitecture()
            .withOptionalLayers(true)
            .applicationServices("..application..")
            .adapter("nats", "..infrastructure.nats..")
            .adapter("grpc", "..infrastructure.grpc..")
            .adapter("rest", "..infrastructure.rest..")
            .ignoreDependency(resideInAPackage("..infrastructure.mapper.."), resideInAPackage("..infrastructure.."))
        rule.check(importedClasses)
    }
}
