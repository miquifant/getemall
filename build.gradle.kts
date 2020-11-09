/**
 * getemall API project
 *
 * Created by miquifant on 2020-10-17
 */
group = "miquifant.getemall"
version = "$version"

description = "getemall API"

plugins {
  // Apply the Kotlin JVM plugin to add support for Kotlin.
  id("org.jetbrains.kotlin.jvm") version "1.3.41"

  // Apply the application plugin to add support for building a CLI application.
  application
}

apply {
  from("$projectDir/gradle/application.gradle.kts")
  from("$projectDir/gradle/distribution.gradle.kts")
  from("$projectDir/gradle/info.gradle.kts")
  from("$projectDir/gradle/manifest.gradle.kts")
  from("$projectDir/gradle/testing.gradle.kts")
}

repositories {
  jcenter()
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

  // App dependencies
  compile("com.fasterxml.jackson.core", "jackson-databind", "2.10.3")
  compile("com.offbytwo", "docopt", "0.6.0.20150202")
  compile("com.typesafe", "config", "1.3.4")
  compile("io.javalin", "javalin", "3.11.0")
  compile("org.mindrot", "jbcrypt", "0.3m")
  compile("org.slf4j", "slf4j-simple", "1.7.25")
  compile("org.webjars.npm", "vue", "2.6.10")

  // App test dependencies
  testCompile("com.github.stefanbirkner", "system-rules", "1.17.0")
}
