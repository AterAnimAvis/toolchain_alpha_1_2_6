Alpha 1.2.6 Modding Toolchain (Client Side Only)
---

## Why?

> Powered by Why not? &trade;

## How do I use it?

Requirements:
  - [Mod Coder Pack](https://minecraft.fandom.com/wiki/Programs_and_editors/Mod_Coder_Pack): v2.5 (More specifically the `.csv`'s and `minecraft.rgs`)
  - Alpha 1.2.6 Client
  - A clone of this repo

Steps:
  - Copy the `conf` folder from `Mod Coder Pack: v2.5` to the repo root.
  - Copy the `a1.2.6 Client Jar` to `jars\minecraft.jar`
  - Run `gradlew :client:setupUserdev`
  - Setup a Run Configuration:
    - IntelliJ:
      - JDK 8
      - Classpath: `$project.mod.main`
      - Main Class: `ateranimavis.launcher.ClientDevLauncher`
      - VM Arguments: `-Djava.library.path=build/natives/${windows|linux|osx}`
      - Working Directory: `$projectDir`
    
## How do I use this to make a mod?

You can't currently, this is planned.

## How do I improve the source?

Follow the steps above but run `gradlew :client:setupMCPdev` instead.

You'll end up with a SRG named workspace

Make your changes/fixes and then run `gradlew :client:makeMCPPatches`

PR your changed patch files upstream to this repo.