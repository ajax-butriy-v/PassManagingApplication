package com.example.passmanagersvc.pass.arch

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.onionArchitecture
import kotlin.test.Test

internal class PassModuleOnionArchitectureTest {
    @Test
    fun `module should be following valid onion architecture`() {
        val rule = onionArchitecture()
            .withOptionalLayers(true)
            .domainModels("..domain..")
            .applicationServices("..application..")
            .adapter("nats", "..infrastructure.nats..")
            .adapter("mongo", "..infrastructure.mongo..")
            .adapter("kafka", "..infrastructure.kafka..")
            .ignoreDependency(
                resideInAPackage("..infrastructure.."),
                resideInAPackage("..infrastructure..mapper..")
            )

        rule.check(importedClasses)
    }

    companion object {
        private val importedClasses: JavaClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.example.passmanagersvc.pass")
    }
}
