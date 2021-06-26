plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
    maven {
        name = "MinecraftForge Maven"
        url = uri("https://maven.minecraftforge.net/")
    }
}

dependencies {
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.8.0")
    implementation("net.minecraftforge:srgutils:0.4.1")
    implementation("net.minecraftforge:DiffPatch:2.0.5:all")

    /* Mixin Plugin */
    implementation("org.ow2.asm:asm-tree:6.2")
}

gradlePlugin {
    plugins.register("mcp-plugin") {
        id = "mcp-plugin"
        implementationClass = "mcp.MCPPlugin"
    }
    plugins.register("mixin-plugin") {
        id = "mixin-plugin"
        implementationClass = "mixin.MixinGradlePlugin"
    }
}