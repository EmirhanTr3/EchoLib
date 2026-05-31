package cat.emir.echolib

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.div

class Language(val plugin: EchoPlugin, val file: String) {
    lateinit var loader: YamlConfigurationLoader
    lateinit var rootNode: CommentedConfigurationNode

    private lateinit var resourceLoader: YamlConfigurationLoader
    private lateinit var resourceRootNode: CommentedConfigurationNode

    fun getOrNull(vararg path: String): String? {
        return rootNode.node(*path).string ?: resourceRootNode.node(*path).string
    }

    fun get(vararg path: String): String {
        return getOrNull(*path) ?: path.joinToString(".")
    }

    fun load() {
        val logger = plugin.slF4JLogger

        val configPath = this.plugin.dataPath / file
        if (!Files.exists(configPath)) {
            logger.info("Generating language from plugin...")
            try {
                this.plugin.getResource(file).use { inputStream ->
                    if (inputStream == null) {
                        logger.error("There is no $file packaged in the plugin.")
                        return
                    }

                    Files.createDirectories(configPath.parent)
                    Files.copy(inputStream, configPath, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: IOException) {
                logger.error("An error occured while copying the language from the plugin:", e)
                return
            }
        }

        logger.info("Loading language file...")
        loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build()

        resourceLoader = YamlConfigurationLoader.builder()
            .source { this.plugin.getResource(file)!!.bufferedReader() }
            .build()

        try {
            rootNode = loader.load()
            resourceRootNode = resourceLoader.load()
        } catch (e: IOException) {
            logger.error("An error occured while loading the language file:", e)
        }
    }
}