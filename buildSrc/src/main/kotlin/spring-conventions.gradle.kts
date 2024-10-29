plugins {
    id("kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.projectreactor:reactor-test:3.5.11") {
        exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
