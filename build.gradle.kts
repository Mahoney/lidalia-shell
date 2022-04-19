plugins {
  id("org.jetbrains.kotlin.jvm") version "1.6.21" apply false
  id("com.autonomousapps.dependency-analysis") version "1.1.0"
  id("org.jmailen.kotlinter") version "3.10.0"
  id("com.vanniktech.dependency.graph.generator") version "0.7.0"
  id("com.dorongold.task-tree") version "2.1.0"
  id("com.github.ben-manes.versions") version "0.42.0"
}

val rootBuildDir = buildDir

subprojects {

  val relativeProjectPath = rootProject.projectDir.toPath().relativize(this.projectDir.toPath())
  buildDir = rootProject.file("${rootBuildDir}/$relativeProjectPath")
}

dependencyAnalysis {
  issues {
    all {
      onAny {
        severity("fail")
      }
    }
  }
}
