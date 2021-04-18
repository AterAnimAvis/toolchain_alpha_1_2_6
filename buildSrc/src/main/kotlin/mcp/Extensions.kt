package mcp

import org.gradle.api.InvalidUserDataException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import task.ApplySpecialSource

fun Project.reobfFinalized(task: Jar) = task.finalizedBy(reobf(task))

fun Project.reobf(task: Jar) = reobf(task.name)

@Throws(InvalidUserDataException::class)
fun Project.reobf(name: String) = reobf.create(name)

val Project.reobf
    get() = extensions.getByName<NamedDomainObjectContainer<ApplySpecialSource>>("reobf")