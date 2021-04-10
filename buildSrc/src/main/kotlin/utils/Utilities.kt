package utils

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ExternalModuleDependency
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.Comparator

object Utilities {

    //------------------------------------------------------------------------------------------------------------------
    // Constants
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Seconds since the unix epoch from unix time (accounting leap years etc) at 1/January/2000 GMT
     */
    private const val JANUARY_FIRST_2000 = 0x386D4380L

    /**
     * Assertion test for Magic Constant
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val time = Instant.parse("2000-01-01T00:00:00.00Z")
        assertEquals(JANUARY_FIRST_2000, time.epochSecond)
    }

    private fun assertEquals(a: Long, b: Long) {
        if (a != b) throw AssertionError(String.format("%s != %s", a, b))
    }

    //------------------------------------------------------------------------------------------------------------------
    // Copy Zip Entries
    //------------------------------------------------------------------------------------------------------------------

    @Throws(IOException::class)
    fun copyZipEntries(zout: ZipOutputStream, zin: ZipInputStream, filter: (ZipEntry)->Boolean) {
        var entry: ZipEntry?
        while (zin.nextEntry.also { entry = it } != null) {
            if (!filter(entry!!)) continue

            val copy = ZipEntry(entry!!.name)
            copy.time = JANUARY_FIRST_2000
            zout.putNextEntry(copy)
            IOUtils.copy(zin, zout)
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Extract Zip with Filter
    //------------------------------------------------------------------------------------------------------------------

    @Throws(IOException::class)
    fun extractZip(source: File, target: File, filter: (ZipEntry)->Boolean, overwrite: Boolean = true, deleteExtras: Boolean = true) {
        val extra: MutableSet<File> = if (deleteExtras) target.collectAll() else mutableSetOf()

        ZipFile(source).use { zip ->
            zip.forEachRemainingFile(filter) { e ->
                val out = File(target, e.name)

                out.ensureParentDirectoriesExist()

                extra.remove(out)

                if (out.exists() && (!overwrite || out.contentsEqual(zip, e)))
                    return@forEachRemainingFile

                FileOutputStream(out).use { fos -> IOUtils.copy(zip.getInputStream(e), fos) }
            }
        }

        if (deleteExtras) {
            extra.forEach { it.delete() }

            target.deleteEmptyDirectories()
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Maven Resolve
    //------------------------------------------------------------------------------------------------------------------

    private val COUNTER: MutableMap<Project, Int> = mutableMapOf()
    private val CACHE: Cache<String, File> = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build()

    fun resolve(project: Project, artifact: String, isChanging: Boolean = false): File? {
        val result : File? = CACHE.getIfExists(artifact) ?: resolveInternal(project, artifact, isChanging)

        if (result != null) CACHE.put(artifact, result)

        return result
    }

    private fun resolveInternal(project: Project, artifact: String, changing: Boolean): File? {
        var name = "mavenDownloader_" + artifact.replace(':', '_')

        synchronized(project) {
            name += "_" + COUNTER.increment(project)
        }

        return project.configurations.temporary(name) { configuration ->
            configuration.withDependency(project, artifact, changing)
            configuration.caching(minutes = 5)

            // We only want the first, not transitive
            return@temporary configuration.resolveOrNull(project, artifact)?.first()
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Private Utility Methods
    //------------------------------------------------------------------------------------------------------------------

    private fun <T : Any> Cache<T, File>.getIfExists(key: T) : File? {
        val result : File = getIfPresent(key) ?: return null

        if (result.exists()) return result

        CACHE.invalidate(key)
        return null
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun <T : Any> MutableMap<T, Int>.increment(key: T) : Int {
        val result = getOrDefault(key, 0).inc()
        put(key, result)
        return result
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun <T> ConfigurationContainer.temporary(name: String, body: (Configuration) -> T) : T {
        val config = create(name)
        val ret = body(config)
        remove(config)

        return ret
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Configuration.withDependency(project: Project, artifact: String, isChanging: Boolean) {
        val dependency = project.dependencies.create(artifact) as ExternalModuleDependency
        dependency.isChanging = isChanging

        dependencies.add(dependency)
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Configuration.caching(minutes: Int) {
        resolutionStrategy {
            cacheChangingModulesFor(minutes, TimeUnit.MINUTES)
            cacheDynamicVersionsFor(minutes, TimeUnit.MINUTES)
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Configuration.resolveOrNull(project: Project, artifact: String) : Set<File>? {
        return try {
            resolve()
        } catch (npe: NullPointerException) {
            project.logger.error("Failed to download $artifact gradle exploded")

            null
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun ZipFile.forEachRemainingFile(filter: (ZipEntry)->Boolean, callback: (ZipEntry) -> Unit) {
        entries().iterator().forEachRemaining { e ->
            if (e.isDirectory || !filter.invoke(e)) return@forEachRemaining

            callback(e)
        }
    }

}