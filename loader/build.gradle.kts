import java.text.SimpleDateFormat
import java.util.Date
import mcp.reobfFinalized

plugins {
    `java-library`
}

apply(plugin = "mcp-plugin")

group = "org.example"
version = "1.0-SNAPSHOT"

//----------------------------------------------------------------------------------------------------------------------
// Mappings
//----------------------------------------------------------------------------------------------------------------------

val generateMcp2Srg by project(":_mappings").tasks.getting(task.srg.TransformSrg::class)

//----------------------------------------------------------------------------------------------------------------------
// MCP
//----------------------------------------------------------------------------------------------------------------------

val jar = tasks.getByName<Jar>("jar")
val reobfJar = project.reobfFinalized(jar)

//----------------------------------------------------------------------------------------------------------------------
// Dependencies
//----------------------------------------------------------------------------------------------------------------------

repositories {
    // Maven Central + Mojang Maven are both provided by mcp-plugin

    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
}

val api : Configuration by configurations.getting

dependencies {
    api(project(":client"))

    //------------------------------------------------------------------------------------------------------------------
    // Common Dev
    //------------------------------------------------------------------------------------------------------------------
    api(group = "org.jetbrains", name = "annotations", version = "20.1.0")

    api(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.11.2")
    runtimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.11.2")

    //------------------------------------------------------------------------------------------------------------------
    // Launcher Implementation
    //------------------------------------------------------------------------------------------------------------------

    implementation(group = "cpw.mods", name = "modlauncher", version = "8.0.9")
    implementation(group = "cpw.mods", name = "grossjava9hacks", version = "1.3.3")
    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")
    implementation(group = "com.google.guava", name = "guava", version = "30.1.1-jre")
    runtimeOnly(group = "com.google.code.gson", name = "gson", version = "2.2.4")
    runtimeOnly(group = "org.ow2.asm", name = "asm-analysis", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-commons", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-tree", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-util", version = "7.2")

    //------------------------------------------------------------------------------------------------------------------
    // Mixins
    //------------------------------------------------------------------------------------------------------------------

    api(group = "org.spongepowered", name = "mixin", version = "0.8.2")
    annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.2", classifier = "processor")
}

//----------------------------------------------------------------------------------------------------------------------
// Mixins
//----------------------------------------------------------------------------------------------------------------------

val compileJava by tasks.getting(JavaCompile::class) {
    dependsOn(generateMcp2Srg)

    //TODO: refmap -> into Jar
    options.compilerArgs.addAll(listOf(
        "-AreobfTsrgFile=${generateMcp2Srg.output.canonicalPath}",
        "-AoutTsrgFile=${file("$temporaryDir/$name-mappings.tsrg").canonicalPath}",
        "-AoutRefMapFile=${file("build/temp/refmap.json").canonicalPath}", // ${file("$temporaryDir/$name-refmap.json").canonicalPath}",
        "-AmappingTypes=tsrg",
        "-AdefaultObfuscationEnv=notch"
    ))
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Specification-Title" to "Launcher",
            "Specification-Vendor" to "AterAnimAvis",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to  "AterAnimAvis",
            "Implementation-Timestamp" to Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
            "Implementation-Timestamp" to Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
            "MixinConfigs" to configs("launcher.mixins.json", "launcher.vanity.mixins.json")
        )
    }
}

fun configs(vararg configs: String) = configs.joinToString(",")
fun Date.format(format: String) = SimpleDateFormat(format).format(this)
