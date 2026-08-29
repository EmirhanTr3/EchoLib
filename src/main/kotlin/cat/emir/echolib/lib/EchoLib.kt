package cat.emir.echolib.lib

import cat.emir.echolib.plugin.EchoPaperPlugin
import cat.emir.echolib.plugin.EchoPlugin
import net.kyori.adventure.text.minimessage.MiniMessage

open class EchoLib {
    companion object {
        internal val miniMessage = MiniMessage.miniMessage()

        lateinit var instance: EchoLib
            private set

        fun init(plugin: EchoPlugin) {
            if (::instance.isInitialized) throw IllegalStateException("EchoLib is already initialized!")

            instance = when (plugin) {
                is EchoPaperPlugin -> EchoLibPaper(plugin)
                else -> throw IllegalStateException("${plugin.javaClass.name} is not a supported plugin class")
            }

            plugin.logger.info("Initialized EchoLib.")
        }
    }
}