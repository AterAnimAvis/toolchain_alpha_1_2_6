package task

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec
import utils.ensureParentDirectoriesExist
import java.io.File

open class ApplyForgeFlower : MavenJarExec() {

    @InputFile
    lateinit var input: File

    @OutputFile
    lateinit var output: File

    init {
        toolJar = "net.minecraftforge:forgeflower:1.5.478.19"
        args = mutableListOf(
                /* Original
                "-din=1",
                "-rbr=1",
                "-dgs=1",
                "-asc=1",
                "-rsy=1",
                "-iec=1",
                "-jvn=1",
                "-isl=0",
                "-iib=1",
                 */
                //----    // Modified | Name
                "-din=1", //   Decompile Inner Classes
                "-rbr=1", //   Remove Bridge Methods
                "-dgs=1", //   Decompile Genetic Signatures
                "-asc=1", //   Ascii String Characters
                "-rsy=0", // X Remove Synthetic
                "-iec=1", //   Include Entire Classpath
                "-jvn=1", //   Use JAD Variable Naming
                "-isl=0", //   Inline Simple Lambda
                "-iib=1", //   Ignore Invalid Bytecode
                "-den=1", // X Decompile Enum
                //----
                "-log=TRACE",
                "{input}",
                "{output}"
        )
        jvmArgs = mutableListOf("-Xmx4G")
    }

    override fun filterArgs(): List<String> {
        output.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to input.absolutePath,
                "{output}" to output.absolutePath
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }
    }

}
