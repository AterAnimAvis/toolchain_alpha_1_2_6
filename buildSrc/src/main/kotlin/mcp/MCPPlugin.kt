package mcp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactRepositoryContainer
import org.gradle.api.artifacts.UnknownRepositoryException
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.the
import task.ApplySpecialSource
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

        project.repositories.maybeDeclare(MIXIN_MAVEN) {
            url = URI("https://repo.spongepowered.org/repository/maven-public/")
            content {
                includeGroup("org.spongepowered")
            }
        }

        val container = project.container(ApplySpecialSource::class.java) { jarName ->
            val name = jarName.capitalize()
            val java = project.the<JavaPluginConvention>()

            val task = project.tasks.maybeCreate("reobf$name", ApplySpecialSource::class.java)
            task.classpath = java.sourceSets.getByName("main").compileClasspath
            task.group = "reobf-tasks"

            val generateMcp2Obf = project.findProject(":_mappings")?.tasks?.getByName("generateMcp2Obf")
            if (generateMcp2Obf != null) {
                task.mappings = generateMcp2Obf.outputs.files.singleFile
            }

            project.tasks.getByName("assemble").dependsOn(task)

            // do after-Evaluate resolution, for the same of good error reporting
            project.afterEvaluate {
                val jar = project.tasks.getByName(jarName)
                if (jar !is Jar) throw IllegalStateException("$jarName is not a jar task. Can only reobf jars!")
                val jarFile = jar.archiveFile.get().asFile
                task.input = jarFile
                task.output = file(jarFile.toPath().resolveSibling("${jarFile.nameWithoutExtension}-reobf.jar"))
                task.dependsOn(jar)

                if (generateMcp2Obf != null && task.mappings == generateMcp2Obf.outputs.files.singleFile) {
                    task.dependsOn(generateMcp2Obf) // Add needed dependency if uses default mappings
                }
            }

            task
        }

        project.extensions.add("reobf", container)
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
        const val MIXIN_MAVEN = "SpongePowered Mixin Maven"
        const val MINECRAFT_MAVEN = "Minecraft Maven"
    }
}