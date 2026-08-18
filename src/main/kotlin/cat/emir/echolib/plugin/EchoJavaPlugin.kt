package cat.emir.echolib.plugin

import org.bukkit.plugin.java.JavaPlugin

abstract class EchoJavaPlugin : EchoPlugin, JavaPlugin() {
    override fun getDataPath() = super<JavaPlugin>.dataPath
    override fun getSLF4JLogger() = super<JavaPlugin>.slF4JLogger
}