package cat.emir.echolib.plugin

import net.kyori.adventure.text.minimessage.MiniMessage
import java.io.File
import java.io.InputStream
import java.nio.file.Path

interface EchoPlugin {
    companion object {
        val miniMessage = MiniMessage.miniMessage()
    }

    fun getDataPath(): Path
    fun getDataFolder(): File
    fun getLogger(): java.util.logging.Logger
    fun getSLF4JLogger(): org.slf4j.Logger
    fun getResource(filename: String): InputStream?
}

inline val EchoPlugin.dataPath get() = this.getDataPath()
inline val EchoPlugin.dataFolder get() = this.getDataFolder()
inline val EchoPlugin.logger get() = this.getLogger()
inline val EchoPlugin.slF4JLogger get() = this.getSLF4JLogger()