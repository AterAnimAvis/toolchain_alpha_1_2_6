plugins {
    `java-library`
    id("uk.jamierocks.propatcher") version "1.3.2" apply false
}

apply(plugin = "mcp-plugin")

//----------------------------------------------------------------------------------------------------------------------
// MCP Configuration
//----------------------------------------------------------------------------------------------------------------------

val clientRetroGuard         = rootProject.file("conf/minecraft.rgs")
val client                   = rootProject.file("jars/minecraft.jar")
val clientSS                 = file("build/temp/minecraft_ss.jar")
val clientInjected           = file("build/temp/minecraft_in.jar")
val clientForgeFlower        = file("build/temp/minecraft_ff.jar")
val clientFiltered           = file("build/temp/minecraft_ft.jar")
val clientSrg2Source         = file("build/temp/minecraft_s2s.jar")

val clientSource             = file("src/main/java")
val clientResources          = file("src/main/resources")
val clientPatches            = rootProject.file("patches/mcp/minecraft")

val clientAccesses           = rootProject.file("mcp-extra/access.txt")
val clientConstructors       = rootProject.file("mcp-extra/constructors.txt")
val clientExceptions         = rootProject.file("mcp-extra/exceptions.txt")

val mappingsFieldsCsv        = mutableSetOf(rootProject.file("conf/fields.csv"), rootProject.file("mcp-extra/fields.csv"))
val mappingsMethodsCsv       = mutableSetOf(rootProject.file("conf/methods.csv"), rootProject.file("mcp-extra/methods.csv"))

val obf2srg                  = file("build/temp/obf_to_srg.tsrg")
val srg2srg                  = file("build/temp/srg_to_srg.tsrg")
val srg2mcp                  = file("build/temp/srg_to_mcp.tsrg")
val rangeMap                 = file("build/temp/range_map.map")

//----------------------------------------------------------------------------------------------------------------------
// Mappings
//----------------------------------------------------------------------------------------------------------------------

val generateObf2Srg by tasks.creating(task.srg.GenerateRetroGuardTSrg::class) {
    group = "srg"

    input = clientRetroGuard
    output = obf2srg
}

val generateSrg2Srg by tasks.creating(task.srg.TransformSrg::class) {
    group = "srg"
    dependsOn(generateObf2Srg)

    input = obf2srg
    output = srg2srg
    transformer = { obf2srg -> obf2srg.reverse().chain(obf2srg) }
}

val generateSrg2Mcp by tasks.creating(task.srg.GenerateMappedSrg::class) {
    group = "srg"
    dependsOn(generateSrg2Srg)

    input = generateSrg2Srg.output
    output = srg2mcp
    fields = mappingsFieldsCsv
    methods = mappingsMethodsCsv
}

//----------------------------------------------------------------------------------------------------------------------
// MCP
//----------------------------------------------------------------------------------------------------------------------

val deobfuscateMC by tasks.creating(task.ApplySpecialSource::class) {
    group = "mcp"
    dependsOn(generateObf2Srg)

    input = client
    output = clientSS
    mappings = generateObf2Srg.output
}

val injectedMC by tasks.creating(task.ApplyMCInjector::class) {
    group = "mcp"
    dependsOn(deobfuscateMC)

    input = deobfuscateMC.output
    output = clientInjected

    access = clientAccesses
    constructors = clientConstructors
    exceptions = clientExceptions
}

val decompMC by tasks.creating(task.ApplyForgeFlower::class) {
    group = "mcp"
    dependsOn(injectedMC)

    input = clientInjected
    output = clientForgeFlower
}

val filterJar by tasks.creating(task.FilterZip::class) {
    group = "mcp"
    dependsOn(decompMC)

    input = decompMC.output
    output = clientFiltered
    filter = { entry -> entry.name.contains("net/minecraft/") }
}

val extractJar by tasks.creating(task.ExtractZip::class) {
    group = "mcp"
    dependsOn(filterJar)

    input = filterJar.output
    output = clientSource
}

val extractResources by tasks.creating(task.ExtractZip::class) {
    group = "mcp"

    input = client
    output = clientResources
    blacklist { it.name.contains(".class") || it.name.contains(".java") || it.name == "null" || it.name.contains("META-INF") }
}

val applyMCPPatches by tasks.creating(uk.jamierocks.propatcher.task.ApplyPatchesTask::class) {
    group = "mcp"
    dependsOn(extractJar)

    target  = clientSource
    patches = clientPatches
}

val extractRangeMap by tasks.creating(task.srg2source.ExtractRangeMap::class) {
    group = "mcp"
    dependsOn(applyMCPPatches)

    outputs.upToDateWhen { false }

    input  = clientSource
    output = rangeMap
    dependencies.add(tasks.getByName<JavaCompile>("compileJava").classpath)
}

val applyS2S by tasks.creating(task.srg2source.ApplyRangeMap::class) {
    group = "mcp"
    dependsOn(extractRangeMap, generateSrg2Mcp)

    outputs.upToDateWhen { false }

    input    = clientSource
    output   = clientSrg2Source
    range    = extractRangeMap.output
    mappings = generateSrg2Mcp.output
}

val extractS2S by tasks.creating(task.ExtractZip::class) {
    group = "mcp"
    dependsOn(applyS2S)

    input = applyS2S.output
    output = clientSource
}

val makeMCPPatches by tasks.creating(uk.jamierocks.propatcher.task.MakePatchesTask::class) {
    group = "mcp"

    root    = filterJar.output
    target  = clientSource
    patches = clientPatches
}

//----------------------------------------------------------------------------------------------------------------------
// Dependencies
//----------------------------------------------------------------------------------------------------------------------

repositories {
    // Maven Central + Mojang Maven are both provided by mcp-plugin
}

val api : Configuration by configurations.getting

dependencies {
    //------------------------------------------------------------------------------------------------------------------
    // Minecraft
    //------------------------------------------------------------------------------------------------------------------

    api(group = "org.lwjgl.lwjgl", name = "lwjgl", version = "2.9.1")
    api(group = "org.lwjgl.lwjgl", name = "lwjgl_util", version = "2.9.1")

    api(group = "net.java.jinput", name = "jinput", version = "2.0.5")
    api(group = "net.java.jutils", name = "jutils", version = "1.0.0")

    api(group = "com.paulscode", name = "codecjorbis", version = "20101023")
    api(group = "com.paulscode", name = "codecwav", version = "20101023")
    api(group = "com.paulscode", name = "soundsystem", version = "20120107")
    api(group = "com.paulscode", name = "libraryjavasound", version = "20101123")
    api(group = "com.paulscode", name = "librarylwjglopenal", version = "20100824")
}

//----------------------------------------------------------------------------------------------------------------------
// Natives
//----------------------------------------------------------------------------------------------------------------------

val natives : Configuration by configurations.creating
val platforms = arrayOf("windows", "linux", "osx")

dependencies {
    platforms.forEach {
        natives(group = "org.lwjgl.lwjgl", name = "lwjgl-platform", version = "2.9.1", classifier = "natives-$it")
        natives(group = "net.java.jinput", name = "jinput-platform", version = "2.0.5", classifier = "natives-$it")
    }
}

platforms.forEach { platform ->
    tasks.create("${platform}Natives", Copy::class) {
        group = "natives"

        val outputDir = "${rootProject.buildDir}/natives/$platform"

        natives.resolvedConfiguration.resolvedArtifacts
            .filter { it.classifier == "natives-$platform" }
            .forEach { from(zipTree(it.file)) }

        into(outputDir)
    }
}

val copyNatives by tasks.creating(DefaultTask::class) {
    group = "natives"
    description = "Copies native libraries to an appropriate directory."
    dependsOn(platforms.map { "${it}Natives" }.map { tasks[it] })
}

//----------------------------------------------------------------------------------------------------------------------
// Setup
//----------------------------------------------------------------------------------------------------------------------

val setupUserdev by tasks.creating(DefaultTask::class) {
    group = "setup-tasks"
    dependsOn(extractS2S, extractResources, copyNatives)
}

val setupMCPdev by tasks.creating(DefaultTask::class) {
    group = "setup-tasks"
    dependsOn(applyMCPPatches, extractResources, copyNatives)
}
