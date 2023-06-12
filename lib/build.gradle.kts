@file:Suppress("UnstableApiUsage")

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.8.22"
  `java-library`
  id("org.jmailen.kotlinter") version "3.10.0"
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")

  testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
  testImplementation("io.kotest:kotest-assertions-core:5.5.5")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }

  consistentResolution {
    useCompileClasspathVersions()
  }

  configurations.all {
    resolutionStrategy {
      failOnNonReproducibleResolution()
    }
  }
}

kotlinter {
  reporters = arrayOf("checkstyle", "plain", "html")
}

testing {
  suites {
    @Suppress("UNUSED_VARIABLE")
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter("5.8.2")
    }
  }
}

tasks {
  register("downloadDependencies") {
    project.configurations.resolveAll()
    project.buildscript.configurations.resolveAll()
  }
}

fun Configuration.isDeprecated(): Boolean =
  this is org.gradle.internal.deprecation.DeprecatableConfiguration && resolutionAlternatives != null

fun ConfigurationContainer.resolveAll() = this
  .filter { it.isCanBeResolved && !it.isDeprecated() }
  .forEach { it.resolve() }
