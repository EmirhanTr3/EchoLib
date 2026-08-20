package cat.emir.echolib.config.multi

import cat.emir.echolib.config.AbstractEchoConfig
import cat.emir.echolib.plugin.EchoPlugin
import cat.emir.echolib.plugin.dataPath
import cat.emir.echolib.plugin.slF4JLogger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

abstract class AbstractMultiFileEchoConfig<T: AbstractEchoConfig>(
    val plugin: EchoPlugin,
    val directory: String,
    val builtinFiles: List<String>? = null
){
    private val _configs = mutableMapOf<String, T>()
    val configs: Map<String, T>
        get() = _configs

    fun load() {
        val allFileNames = mutableListOf<String>()

        if (!Files.exists(plugin.dataPath / directory)) {
            if (!builtinFiles.isNullOrEmpty())
                Files.createDirectories(plugin.dataPath / directory)
            return
        }

        builtinFiles?.forEach { file ->
            val configPath = plugin.dataPath / directory / file
            if (!Files.exists(configPath)) {
                try {
                    plugin.getResource("$directory/$file").use { inputStream ->
                        if (inputStream == null) {
                            plugin.slF4JLogger.error("There is no $directory/$file packaged in the plugin.")
                            return
                        }

                        Files.createDirectories(configPath.parent)
                        Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                } catch (e: IOException) {
                    plugin.slF4JLogger.error("An error occured while copying $directory/$file from the plugin:", e)
                    return
                }
            }
        }

        Files.walk(plugin.dataPath / directory).use { paths ->
            paths.forEach { path ->
                if (path.isDirectory()) return@forEach

                val fileName = path.fileName.pathString
                val noExtFileName = fileName.split(".").dropLast(1).joinToString(".")
                allFileNames.add(noExtFileName)

                if (_configs.containsKey(noExtFileName)) {
                    _configs[noExtFileName]!!.load()
                } else {
                    val config = buildEchoConfig("$directory/$fileName")
                    _configs[noExtFileName] = config
                    config.load()
                }
            }
        }

        _configs.keys
            .filter { !allFileNames.contains(it) }
            .forEach { _configs.remove(it) }
    }

    abstract fun buildEchoConfig(name: String): T
}