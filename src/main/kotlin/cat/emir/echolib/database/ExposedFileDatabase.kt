package cat.emir.echolib.database

import cat.emir.echolib.EchoPlugin
import org.jetbrains.exposed.v1.jdbc.Database

abstract class ExposedFileDatabase(plugin: EchoPlugin, name: String) : AbstractFileDatabase(plugin, name) {
    val database: Database = Database.connect(dataSource)
}