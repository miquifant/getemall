/**
 * Configuration for distributing application.
 *
 * Created by miquifant on 2020-10-18
 */
// Disable distTar task
tasks.named<Tar>("distTar") {
  onlyIf { false }
}

// Construct 'zip' adding configuration and laucher script from the project folder
tasks.named<Zip>("distZip") {
  into (project.name + "-" + project.version) {
    from(".")
    include("bin/getemall-api")
    include("conf/*")
  }
}
