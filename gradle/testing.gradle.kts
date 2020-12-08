/**
 * Testing configuration.
 *
 * Created by miquifant on 2020-10-17
 */
tasks.named<Test>("test") {

  // Don't show standard out and standard error of the test JVM(s) on the console, and other verbose info
  testLogging.showStandardStreams = false
  testLogging.showExceptions      = false
  testLogging.showStackTraces     = false
  testLogging.showCauses          = false

  // Don't fail the 'test' task on the first test failure
  failFast = false

  // Configure environment variables for testing (as if they were initialized by start script)
  environment.putAll(mapOf(
      "GETEMALL_HOME" to "$projectDir",
      "GETEMALL_CONF" to "$projectDir/src/test/resources/conf"
  ))
  // Configure system properties (not used, but just in case we need some special local library)
  systemProperties.putAll(mapOf(
      "java.library.path" to "$projectDir/lib"
  ))

  // Format output to show, for each test:
  // - <class>.<test> with result: <SUCCESS or FAILURE>
  afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->

    logger.quiet("  - ${desc.className}.${desc.name} with result: ${result.resultType}")
    if (result.resultType == TestResult.ResultType.FAILURE && result.exception != null) {
      logger.quiet("    > ${(result.exception as Throwable).message}")
    }
    Unit
  }))
}
