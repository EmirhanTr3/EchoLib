package cat.emir.echolib.lib

import cat.emir.echolib.cache.OfflinePlayerCache
import cat.emir.echolib.plugin.EchoPaperPlugin

internal class EchoLibPaper(plugin: EchoPaperPlugin) : EchoLib() {
    val offlinePlayerCache = OfflinePlayerCache(plugin).also { it.load() }
}

internal val EchoLib.paper get() = this as EchoLibPaper