package cat.emir.echolib

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

abstract class EchoPlugin : JavaPlugin() {
    companion object {
        val miniMessage = MiniMessage.miniMessage()
    }
}