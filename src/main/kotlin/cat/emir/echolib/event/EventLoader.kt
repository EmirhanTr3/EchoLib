package cat.emir.echolib.event

import cat.emir.echolib.EchoPlugin
import cat.emir.echolib.utils.ClassUtils
import org.bukkit.Bukkit

class EventLoader {
    companion object {
        fun registerEvents(plugin: EchoPlugin, pkg: String) {
            ClassUtils.findClasses(
                plugin = plugin,
                pkg = pkg,
                condition = { it.extendsSuperclass(EchoEvent::class.java) },
                function = {
                    val event = it.loadClass().constructors[0].newInstance(plugin) as EchoEvent
                    Bukkit.getPluginManager().registerEvents(event, plugin)
                }
            )
        }
    }
}