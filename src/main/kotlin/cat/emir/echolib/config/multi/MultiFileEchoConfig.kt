package cat.emir.echolib.config.multi

import cat.emir.echolib.config.EchoConfig
import cat.emir.echolib.plugin.EchoPlugin
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class MultiFileEchoConfig(
    plugin: EchoPlugin,
    directory: String,
    builtinFiles: List<String>? = null,
    val loaderModifier: ((YamlConfigurationLoader.Builder) -> YamlConfigurationLoader.Builder)? = null
) : AbstractMultiFileEchoConfig<EchoConfig>(plugin, directory, builtinFiles) {

    override fun buildEchoConfig(name: String) = EchoConfig(plugin, name, loaderModifier)
}