package cat.emir.echolib

import cat.emir.echolib.extensions.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.div

fun CommandSender.sendLangMessage(path: String) {
    if (!Language.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(Language.instance.get(path).toComponent())
}

fun CommandSender.sendLangMessage(path: String, data: List<Pair<String, String>>) {
    if (!Language.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(Language.instance.get(path, data))
}

fun Server.sendLangMessage(path: String) {
    if (!Language.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(Language.instance.get(path).toComponent())
}

fun Server.sendLangMessage(path: String, data: List<Pair<String, String>>) {
    if (!Language.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(Language.instance.get(path, data))
}

class Language(val plugin: EchoPlugin, val file: String) {

    companion object {
        lateinit var instance: Language
            private set

        val isInitialized: Boolean
            get() = ::instance.isInitialized
    }

    init {
        instance = this
    }

    lateinit var loader: YamlConfigurationLoader
    lateinit var rootNode: CommentedConfigurationNode

    lateinit var resourceLoader: YamlConfigurationLoader
    lateinit var resourceRootNode: CommentedConfigurationNode

    val all: Map<String, String>
        get() = internalAll.toMap()
    private var internalAll = mapOf<String, String>()

    fun getOrNull(path: String): String? {
        return rootNode.node(path.split(".")).string ?: resourceRootNode.node(path.split(".")).string
    }

    fun get(path: String): String {
        return getOrNull(path) ?: "<error>Missing language key: <fatal>$path<fatal></error>"
    }

    fun get(path: String, data: List<Pair<String, String>>): Component {
        return get(path)
            .toComponent(TagResolver.resolver(data.map { Placeholder.unparsed(it.first, it.second) }))
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

        internalAll = findAllLangKeys()
    }

    private fun findAllLangKeys(): Map<String, String> {
        val rootNode = findLangKeys(rootNode.childrenMap())
        val resourceRootNode = findLangKeys(resourceRootNode.childrenMap())

        return resourceRootNode + rootNode
    }

    private fun <N : ScopedConfigurationNode<N>> findLangKeys(map: Map<Any, N>, currentScopes: List<String> = emptyList()): Map<String, String> {
        val resultMap = mutableMapOf<String, String>()

        for ((key, value) in map) {
            if (value.string != null) {
                resultMap[(currentScopes + key).joinToString(".")] = value.string!!
            } else {
                resultMap.putAll(findLangKeys(value.childrenMap(), (currentScopes + key.toString())))
            }
        }

        return resultMap
    }
}