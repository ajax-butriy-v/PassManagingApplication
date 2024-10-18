plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("io.github.surpsg:delta-coverage-gradle:2.5.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.2") {
        exclude(module = "mockito-core")
    }
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.9.23")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")
}
