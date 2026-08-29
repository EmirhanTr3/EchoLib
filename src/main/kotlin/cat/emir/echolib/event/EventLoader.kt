package cat.emir.echolib.event

import cat.emir.echolib.plugin.EchoPaperPlugin
import cat.emir.echolib.utils.ClassUtils
import org.bukkit.Bukkit

class EventLoader {
    companion object {
        fun registerListeners(plugin: EchoPaperPlugin, pkg: String) {
            ClassUtils.findClasses(
                plugin = plugin,
                pkg = pkg,
                condition = { it.extendsSuperclass(EchoListener::class.java) },
                function = {
                    val event = it.loadClass().constructors[0].newInstance(plugin) as EchoListener
                    Bukkit.getPluginManager().registerEvents(event, plugin)
                }
            )
        }
    }
}