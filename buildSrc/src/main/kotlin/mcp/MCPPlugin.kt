package mcp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.UnknownRepositoryException
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

@Suppress("unused")
class MCPPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.repositories.hasCentral())
            project.repositories.mavenCentral()

        project.repositories.maybeDeclare(MINECRAFT_FORGE_MAVEN) {
            url = URI("http://files.minecraftforge.net/maven/")
        }

        project.repositories.maybeDeclare(MINECRAFT_MAVEN) {
            url = URI("https://libraries.minecraft.net/")
            content {
                includeGroup("com.paulscode")
            }
        }


    }

    private fun RepositoryHandler.maybeDeclare(name: String, configure: MavenArtifactRepository.() -> Unit) {
        if (!has(name))
            maven {
                this.name = name
                configure()
            }
    }

    private fun RepositoryHandler.hasCentral() = has(ArtifactRepositoryContainer.DEFAULT_MAVEN_CENTRAL_REPO_NAME)

    private fun RepositoryHandler.has(name: String) : Boolean {
        return try {
            getByName(name)

            true
        } catch (e: UnknownRepositoryException) {
            false
        }
    }

    companion object {
        const val MINECRAFT_FORGE_MAVEN = "MinecraftForge Maven"
        const val MINECRAFT_MAVEN = "Minecraft Maven"
    }
}