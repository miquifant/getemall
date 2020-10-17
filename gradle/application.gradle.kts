/**
 * Application and running scripts configuration.
 *
 * Created by miquifant on 2020-10-18
 */
// Define the main class for the application
(extensions.getByName("application") as JavaApplication).apply {

  mainClassName = "miquifant.getemall.Main"
}

// Modify the application's start script
tasks.named<CreateStartScripts>("startScripts") {

  // Change the name of the script
  applicationName = "run-getemall-api"

  // Change the code of the unix script and delete windows one
  doLast {
    val shebang = Regex("#!.*")

    windowsScript.delete()

    unixScript.writeText("""
      |# Don't call this script directly. Use `getemall-api` script instead.
      |#
      |""".trimMargin().trimStart() +
        unixScript.readLines().joinToString("\n") { line ->
          if (line.matches(shebang)) "#!/usr/bin/env bash" else line
        }
    )
  }
}
