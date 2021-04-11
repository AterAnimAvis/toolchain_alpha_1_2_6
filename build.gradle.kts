plugins {
    java
    id("uk.jamierocks.propatcher") version "1.3.2" apply false
}

apply(plugin = "mcp-plugin")

group = "org.example"
version = "1.0-SNAPSHOT"

//----------------------------------------------------------------------------------------------------------------------
// MCP Configuration
//----------------------------------------------------------------------------------------------------------------------

val clientRetroGuard         = file("conf/minecraft.rgs")
val client                   = file("jars/minecraft.jar")
val clientSS                 = file("build/temp/minecraft_ss.jar")
val clientInjected           = file("build/temp/minecraft_in.jar")
val clientForgeFlower        = file("build/temp/minecraft_ff.jar")
val clientFiltered           = file("build/temp/minecraft_ft.jar")
val clientSrg2Source         = file("build/temp/minecraft_s2s.jar")

val clientSource             = file("src/client/java")
val clientResources          = file("src/client/resources")
val clientPatches            = file("patches/mcp/minecraft")

val clientAccesses           = file("mcp-extra/access.txt")
val clientConstructors       = file("mcp-extra/constructors.txt")
val clientExceptions         = file("mcp-extra/exceptions.txt")

val mappingsFieldsCsv        = mutableSetOf(file("conf/fields.csv"), file("mcp-extra/fields.csv"))
val mappingsMethodsCsv       = mutableSetOf(file("conf/methods.csv"), file("mcp-extra/methods.csv"))

val obf2srg                  = file("build/temp/obf_to_srg.tsrg")
val srg2srg                  = file("build/temp/srg_to_srg.tsrg")
val srg2mcp                  = file("build/temp/srg_to_mcp.tsrg")
val mcp2srg                  = file("build/temp/mcp_to_srg.tsrg")
val mcp2obf                  = file("build/temp/mcp_to_obf.tsrg")
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

val generateMcp2Srg by tasks.creating(task.srg.TransformSrg::class) {
    group = "srg"
    dependsOn(generateSrg2Mcp)

    input = generateSrg2Mcp.output
    output = mcp2srg
    transformer = { srg2mcp -> srg2mcp.reverse() }
}

val generateMcp2Obf by tasks.creating(task.srg.GenerateRemappingSrg::class) {
    group = "srg"
    dependsOn(generateSrg2Mcp, generateObf2Srg)

    inputA = generateSrg2Mcp.output
    inputB = generateObf2Srg.output
    output = mcp2obf
    transformer = { srg2mcp, obf2srg ->
        obf2srg.chain(srg2mcp).reverse()
    }
}

//----------------------------------------------------------------------------------------------------------------------
// MCP
//----------------------------------------------------------------------------------------------------------------------

val main : SourceSet by sourceSets.getting {
    java {
        srcDir("src/client/java")
        srcDir("src/launcher/java")
    }
    resources {
        srcDir("src/client/resources")
        srcDir("src/launcher/resources")
    }
}

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
    blacklist { it.name.contains(".class") || it.name.contains(".java") || it.name == "null" }
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

val jar by tasks.getting(Jar::class) {

}

val reobfJar by tasks.creating(task.ApplySpecialSourceInPlace::class) {
    group = "mcp"
    dependsOn(generateMcp2Obf)

    input = jar.archiveFile.get().asFile
    output = jar.archiveFile.get().asFile
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

dependencies {
    //------------------------------------------------------------------------------------------------------------------
    // Minecraft
    //------------------------------------------------------------------------------------------------------------------

    implementation(group = "org.lwjgl.lwjgl", name = "lwjgl", version = "2.9.1")
    implementation(group = "org.lwjgl.lwjgl", name = "lwjgl_util", version = "2.9.1")

    implementation(group = "net.java.jinput", name = "jinput", version = "2.0.5")
    implementation(group = "net.java.jutils", name = "jutils", version = "1.0.0")

    implementation(group = "com.paulscode", name = "codecjorbis", version = "20101023")
    implementation(group = "com.paulscode", name = "codecwav", version = "20101023")
    implementation(group = "com.paulscode", name = "soundsystem", version = "20120107")
    implementation(group = "com.paulscode", name = "libraryjavasound", version = "20101123")
    implementation(group = "com.paulscode", name = "librarylwjglopenal", version = "20100824")

    //------------------------------------------------------------------------------------------------------------------
    // Common Dev
    //------------------------------------------------------------------------------------------------------------------

    implementation(group = "org.jetbrains", name = "annotations", version = "20.1.0")

    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.11.2")
    runtimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.11.2")

    //------------------------------------------------------------------------------------------------------------------
    // Launcher Implementation
    //------------------------------------------------------------------------------------------------------------------

    implementation(group = "cpw.mods", name = "modlauncher", version = "8.0.9")
    implementation(group = "cpw.mods", name = "grossjava9hacks", version = "1.3.3")
    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")
    runtimeOnly(group = "com.google.guava", name = "guava", version = "30.1.1-jre")
    runtimeOnly(group = "com.google.code.gson", name = "gson", version = "2.2.4")
    runtimeOnly(group = "org.ow2.asm", name = "asm-analysis", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-commons", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-tree", version = "7.2")
    runtimeOnly(group = "org.ow2.asm", name = "asm-util", version = "7.2")

    implementation(group = "org.spongepowered", name = "mixin", version = "0.8.1")
    annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.1", classifier = "processor")
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

        val outputDir = "$buildDir/natives/$platform"

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
