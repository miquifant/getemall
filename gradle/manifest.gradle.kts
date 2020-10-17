/**
 * Artifact manifest configuration.
 *
 * Created by miquifant on 2020-10-18
 */
import java.net.InetAddress

val buildUser: String     = System.getProperty("user.name").trim()
val buildDate: Long       = System.currentTimeMillis()
val buildMachine: String? = InetAddress.getLocalHost().canonicalHostName

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
        "Implementation-Title"   to "$buildDate : $buildUser : $buildMachine",
        "Implementation-Version" to project.version
    ))
  }
}
