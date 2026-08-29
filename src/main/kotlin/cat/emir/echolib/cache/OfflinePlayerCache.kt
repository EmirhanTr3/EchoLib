package cat.emir.echolib.cache

import cat.emir.echolib.plugin.EchoPaperPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.ConcurrentHashMap

class OfflinePlayerCache(val plugin: EchoPaperPlugin) : Listener {
    private val cache = ConcurrentHashMap.newKeySet<String>()

    fun load() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.server.offlinePlayers.forEach { it.name?.let(cache::add) }
        })
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        cache.add(event.player.name)
    }

    fun suggestions(prefix: String, limit: Int = 50): List<String> =
        cache.asSequence()
            .filter { it.startsWith(prefix, true) }
            .take(limit)
            .toList()
}