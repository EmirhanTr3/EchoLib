package cat.emir.echolib.config

import cat.emir.echolib.plugin.EchoPlugin
import cat.emir.echolib.plugin.dataPath
import cat.emir.echolib.plugin.slF4JLogger
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.div

abstract class AbstractEchoConfig(val plugin: EchoPlugin, val file: String) {
    lateinit var loader: YamlConfigurationLoader
        private set
    lateinit var rootNode: CommentedConfigurationNode
        private set
    var dataPath = plugin.dataPath
    var finalFile: String = file

    init {
        if (file.contains("/")) {
            val split = file.split("/")
            finalFile = split.last()
            dataPath /= split.dropLast(1).joinToString("/")
        }
    }

    @Deprecated("not really supported but exists, use on your own risk")
    fun save() {
        try {
            loader.save(rootNode)
        } catch (e: ConfigurateException) {
            plugin.slF4JLogger.error("Couldn't save config:", e)
        }
    }

    fun load() {
        val logger = plugin.slF4JLogger

        val configPath = dataPath / finalFile
        if (!Files.exists(configPath)) {
            logger.info("Generating config $file from plugin...")
            try {
                plugin.getResource(file).use { inputStream ->
                    if (inputStream == null) {
                        logger.error("There is no $file packaged in the plugin.")
                        return
                    }

                    Files.createDirectories(configPath.parent)
                    Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: IOException) {
                logger.error("An error occured while copying the config from the plugin:", e)
                return
            }
        }

        logger.info("Loading configuration file $file...")
        loader = buildLoader(configPath)

        try {
            rootNode = loader.load()
        } catch (e: IOException) {
            logger.error("An error occured while loading the configuration:", e)
        }

        finishLoading()
    }

    abstract fun buildLoader(configPath: Path) : YamlConfigurationLoader
    open fun finishLoading() {}
}