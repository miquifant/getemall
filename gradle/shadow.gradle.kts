/**
 * Configuration for distributing application.
 *
 * Created by miquifant on 2020-11-29
 */
// Define the main class for the application
(extensions.getByName("application") as JavaApplication).apply {

  mainClassName = "miquifant.getemall.Main"
}

// Modify the application's start script
tasks.named<CreateStartScripts>("startShadowScripts") {

  // Change the name of the script
  applicationName = "run-getemall-api"

  // Change the code of the unix script and delete windows one
  doLast {
    windowsScript.delete()

    unixScript.writeText("""
      |# Don't call this script directly. Use `getemall-api` script instead.
      |#
      |""".trimMargin().trimStart() +
        unixScript.readText()
    )
  }
}

// Disable distTar, distZip and shadowDistTar task
tasks.named<Tar>("distTar") {
  onlyIf { false }
}
tasks.named<Zip>("distZip") {
  onlyIf { false }
}
tasks.named<Tar>("shadowDistTar") {
  onlyIf { false }
}

// Construct 'zip' adding configuration and laucher script from the project folder
tasks.named<Zip>("shadowDistZip") {
  archiveBaseName.value(project.name)
  into (project.name + "-" + project.version) {
    from(".")
    include("bin/getemall-api")
    include("conf/*")
  }
}
