package cat.emir.echolib.database

import cat.emir.echolib.EchoPlugin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Path
import java.sql.Connection
import kotlin.io.path.div

abstract class FileDatabase(plugin: EchoPlugin, name: String) {
    val file: Path = plugin.dataFolder.toPath() / name

    val dataSource: HikariDataSource = run {
        val config = HikariConfig()

        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:./$file"
        config.maximumPoolSize = 10

        HikariDataSource(config)
    }

    val connection: Connection
        get() = dataSource.connection

    abstract fun load()
}