package com.example.passmanagersvc.passowner.arch

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.onionArchitecture
import kotlin.test.Test

internal class PassOwnerModuleOnionArchitectureTest {
    @Test
    fun `module should be following valid onion architecture`() {
        val rule = onionArchitecture()
            .withOptionalLayers(true)
            .domainModels("..domain..")
            .applicationServices("..application..")
            .adapter("redis", "..infrastructure.redis..")
            .adapter("mongo", "..infrastructure.mongo..")
            .adapter("rest", "..infrastructure.rest..")

        rule.check(importedClasses)
    }

    companion object {
        private val importedClasses: JavaClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.example.passmanagersvc.passowner")
    }
}
