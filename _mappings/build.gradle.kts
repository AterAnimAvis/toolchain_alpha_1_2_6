plugins {
    `java-library`
}

apply(plugin = "mcp-plugin")

group = "org.example"
version = "1.0-SNAPSHOT"

//----------------------------------------------------------------------------------------------------------------------
// MCP Configuration
//----------------------------------------------------------------------------------------------------------------------

val clientRetroGuard         = rootProject.file("conf/minecraft.rgs")

val rgReplacements           = file("mcp-extra/class-renames.csv")
val mappingsFieldsCsv        = mutableSetOf(rootProject.file("conf/fields.csv"), file("mcp-extra/fields.csv"))
val mappingsMethodsCsv       = mutableSetOf(rootProject.file("conf/methods.csv"), file("mcp-extra/methods.csv"))

val obf2srg                  = file("build/temp/obf_to_srg.tsrg")
val srg2srg                  = file("build/temp/srg_to_srg.tsrg")
val srg2mcp                  = file("build/temp/srg_to_mcp.tsrg")
val mcp2srg                  = file("build/temp/mcp_to_srg.tsrg")
val mcp2obf                  = file("build/temp/mcp_to_obf.tsrg")

//----------------------------------------------------------------------------------------------------------------------
// Mappings
//----------------------------------------------------------------------------------------------------------------------

val generateObf2Srg by tasks.creating(task.srg.GenerateRetroGuardTSrg::class) {
    group = "srg"

    input = clientRetroGuard
    renames = rgReplacements
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
