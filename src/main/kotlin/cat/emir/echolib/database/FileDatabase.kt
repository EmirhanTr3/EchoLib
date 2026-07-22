package cat.emir.echolib.database

import cat.emir.echolib.EchoPlugin
import java.sql.Connection

abstract class FileDatabase(plugin: EchoPlugin, name: String) : AbstractFileDatabase(plugin, name) {
    val connection: Connection
        get() = dataSource.connection
}