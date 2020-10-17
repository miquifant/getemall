/**
 * Info tasks.
 *
 * Created by miquifant on 2020-10-17
 */
// Shows info about the project, like group, name, version, description and project directory
tasks.register("info") {

  group = "miquifant"
  description = "Shows information about the project"

  doLast {
    println("""
      |- group.........: ${project.group}
      |- name..........: ${project.name}
      |- version.......: ${project.version}
      |- description...: ${project.description}
      |- projectDir....: ${project.projectDir}
      |""".trimMargin().trim())
  }
}
