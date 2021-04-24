import java.time.Instant
import java.time.format.DateTimeFormatter
import mcp.reobfFinalized

plugins {
    `java-library`
}

apply(plugin = "mcp-plugin")
//TODO: Uncomment below for mixin support
// apply(plugin = "mixin-plugin")

//----------------------------------------------------------------------------------------------------------------------
// Config
//----------------------------------------------------------------------------------------------------------------------

val modId: String by extra
val modName: String by extra
val modVersion: String by extra
val modGroup: String by extra
val vendor: String by extra

//----------------------------------------------------------------------------------------------------------------------
// Common
//----------------------------------------------------------------------------------------------------------------------

group = modGroup
version = modVersion

//----------------------------------------------------------------------------------------------------------------------
// MCP
//----------------------------------------------------------------------------------------------------------------------

val jar = tasks.getByName<Jar>("jar")
val reobfJar = project.reobfFinalized(jar)

//----------------------------------------------------------------------------------------------------------------------
// Dependencies
//----------------------------------------------------------------------------------------------------------------------

repositories {
    // Maven Central + Mojang + SpongePowered Maven are provided by mcp-plugin
}

val api : Configuration by configurations.getting

dependencies {
    api(project(":loader"))

    //------------------------------------------------------------------------------------------------------------------
    // Mixins
    //------------------------------------------------------------------------------------------------------------------

    //TODO: Uncomment below for mixin support
    // annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.1", classifier = "processor")
}

//----------------------------------------------------------------------------------------------------------------------
// Mixins
//----------------------------------------------------------------------------------------------------------------------
//TODO: Uncomment below for mixin support
// the<mixin.MixinExtension>().add(sourceSets["main"], "$modId.refmap.json")

//----------------------------------------------------------------------------------------------------------------------
// Jar Manifest
//----------------------------------------------------------------------------------------------------------------------

tasks.withType<Jar> {
    manifest {
        attributes(
            "Specification-Title" to modName,
            "Specification-Vendor" to vendor,
            "Specification-Version" to "1",
            "Implementation-Title" to modName,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to vendor,
            "Implementation-Timestamp" to Instant.now().iso8601()
            //TODO: Uncomment below for mixin support
            // ,"MixinConfigs" to configs("$modId.mixins.json")
        )
    }
}

fun configs(vararg configs: String) = configs.joinToString(",")
fun Instant.iso8601() = DateTimeFormatter.ISO_INSTANT.format(this)


