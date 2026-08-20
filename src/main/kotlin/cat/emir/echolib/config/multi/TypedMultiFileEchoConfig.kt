package cat.emir.echolib.config.multi

import cat.emir.echolib.config.TypedEchoConfig
import cat.emir.echolib.plugin.EchoPlugin
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.reflect.KClass

class TypedMultiFileEchoConfig<T: Any>(
    val type: KClass<T>,
    plugin: EchoPlugin,
    directory: String,
    builtinFiles: List<String>? = null,
    val loaderModifier: ((YamlConfigurationLoader.Builder) -> YamlConfigurationLoader.Builder)? = null
) : AbstractMultiFileEchoConfig<TypedEchoConfig<T>>(plugin, directory, builtinFiles) {

    override fun buildEchoConfig(name: String) = TypedEchoConfig(type, plugin, name, loaderModifier)
}