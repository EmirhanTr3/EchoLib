package cat.emir.echolib

import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.plugin.EchoJavaPlugin
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.div

fun Audience.sendLangMessage(path: String) {
    if (!EchoLang.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(EchoLang.instance.get(path).toComponent())
}

fun Audience.sendLangMessage(path: String, data: List<Pair<String, String>>) {
    if (!EchoLang.isInitialized) throw UnsupportedOperationException("Language not initialized!")
    this.sendMessage(EchoLang.instance.get(path, data))
}

class EchoLang(val plugin: EchoJavaPlugin, val file: String) {

    companion object {
        lateinit var instance: EchoLang
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

    private fun parseData(data: List<Pair<String, String>>): TagResolver {
        return TagResolver.resolver(data.map { Placeholder.unparsed(it.first, it.second) })
    }

    fun getOrNull(path: String): String? {
        val nodePath = path.split(".")
        var list = rootNode.node(nodePath).getList(String::class.java)
        if (list.isNullOrEmpty()) {
            list = resourceRootNode.node(nodePath).getList(String::class.java)
        }
        if (list.isNullOrEmpty()) {
            return null
        }
        return list.joinToString("\n")
    }

    fun get(path: String): String {
        return getOrNull(path) ?: "<error>Missing language key: <fatal>$path</fatal></error>"
    }

    fun get(path: String, data: List<Pair<String, String>>): Component {
        return get(path).toComponent(parseData(data))
    }

    fun load() {
        val logger = plugin.slF4JLogger

        if (this.plugin.getResource(file) == null) {
            logger.error("There is no $file packaged in the plugin.")
            return
        }

        val configPath = this.plugin.dataPath / file
        if (!Files.exists(configPath)) {
            logger.info("Generating language from plugin...")
            try {
                Files.createDirectories(configPath.parent)

                val githubRegex = Regex(
                    """https?://github\.com/([a-z\d](?:[a-z\d]|-(?=[a-z\d])){0,38})/([a-z0-9._-]+)(?:/.*)?""",
                    RegexOption.IGNORE_CASE
                )

                val content = mutableListOf(
                    "# This file is a language file for ${plugin.name}.",
                    "# Any keys entered in this file will replace the default ones."
                )

                if (plugin.pluginMeta.website != null && plugin.pluginMeta.website!!.matches(githubRegex)) {
                    val result = githubRegex.find(plugin.pluginMeta.website!!)!!
                    getGithubRepo(result.groupValues[1], result.groupValues[2])?.let {
                        content.add("# All default keys can be found in ${it.htmlUrl}/blob/${it.defaultBranch}/src/main/resources/$file")
                    }
                }

                Files.write(configPath, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
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

    private data class GitHubRepo(
        @SerializedName("html_url") val htmlUrl: String,
        @SerializedName("default_branch") val defaultBranch: String
    )

    private fun getGithubRepo(owner: String, repo: String): GitHubRepo? {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/$owner/$repo"))
            .header("Accept", "application/vnd.github+json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) return null

        return Gson().fromJson(response.body(), GitHubRepo::class.java)
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