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
val generateMcp2Obf by project(":_mappings").tasks.getting(task.srg.GenerateRemappingSrg::class)

//----------------------------------------------------------------------------------------------------------------------
// MCP
//----------------------------------------------------------------------------------------------------------------------

val jar by tasks.getting(Jar::class) {

}

val reobfJar by tasks.creating(task.ApplySpecialSource::class) {
    group = "mcp"
    dependsOn(generateMcp2Obf)

    val jarFile = jar.archiveFile.get().asFile

    input = jarFile
    output = file(jarFile.toPath().resolveSibling("${jarFile.nameWithoutExtension}-reobf.jar"))
    mappings = generateMcp2Obf.output
}

jar.finalizedBy(reobfJar)

//----------------------------------------------------------------------------------------------------------------------
// Dependencies
//----------------------------------------------------------------------------------------------------------------------

repositories {
    // Maven Central + Mojang Maven are both provided by mcp-plugin

    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
}

val api : Configuration by configurations.getting

dependencies {
    api(project(":loader"))

    //------------------------------------------------------------------------------------------------------------------
    // Mixins
    //------------------------------------------------------------------------------------------------------------------

    api(group = "org.spongepowered", name = "mixin", version = "0.8.1")
    annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.1", classifier = "processor")
}

//----------------------------------------------------------------------------------------------------------------------
// Mixins
//----------------------------------------------------------------------------------------------------------------------

val compileJava by tasks.getting(JavaCompile::class) {
    dependsOn(generateMcp2Srg)

    //TODO: refmap -> into Jar
    options.compilerArgs.addAll(listOf(
        "-AreobfTsrgFile=${generateMcp2Srg.output!!.canonicalPath}",
        "-AoutTsrgFile=${file("$temporaryDir/$name-mappings.tsrg").canonicalPath}",
        "-AoutRefMapFile=${file("build/temp/refmap.json").canonicalPath}", // ${file("$temporaryDir/$name-refmap.json").canonicalPath}",
        "-AmappingTypes=tsrg"
    ))
}
