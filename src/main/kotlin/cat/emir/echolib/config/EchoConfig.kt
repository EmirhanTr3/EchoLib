package cat.emir.echolib.config

import cat.emir.echolib.plugin.EchoPlugin
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

class EchoConfig(
    plugin: EchoPlugin,
    file: String,
    val loaderModifier: ((YamlConfigurationLoader.Builder) -> YamlConfigurationLoader.Builder)? = null
) : AbstractEchoConfig(plugin, file) {

    override fun buildLoader(configPath: Path): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(configPath)
            .let { loaderModifier?.invoke(it) ?: it }
            .build()
    }
}
