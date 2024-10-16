plugins {
    kotlin("jvm")
    id("io.github.surpsg.delta-coverage")
    jacoco
}

deltaCoverageReport {
    val targetBranch = project.properties["diffBase"]?.toString() ?: "refs/remotes/origin/main"
    diffSource.byGit {
        useNativeGit = true
        compareWith(targetBranch)
    }

    violationRules.failIfCoverageLessThan(0.6)
    reports {
        html = true
        markdown = true
    }
}