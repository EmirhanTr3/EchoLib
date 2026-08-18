package cat.emir.echolib.config

import cat.emir.echolib.plugin.EchoPlugin
import cat.emir.echolib.plugin.slF4JLogger
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TypedEchoConfig<T : Any>(
    val type: KClass<T>,
    plugin: EchoPlugin,
    file: String,
    val loaderModifier: ((YamlConfigurationLoader.Builder) -> YamlConfigurationLoader.Builder)? = null
) : AbstractEchoConfig(plugin, file) {

    val typedConfig: T
        get() = _typedConfig!!

    private var _typedConfig: T? = null
        set(value) = if (value == null)
            field = value ?: run {
                val ctor = type.primaryConstructor
                    ?: error("${type.simpleName} has no primary constructor")
                require(ctor.parameters.all { it.isOptional }) {
                    "${type.simpleName} must have default values for all constructor parameters to be auto-instantiated"
                }
                ctor.callBy(emptyMap())
            }
        else field = value

    override fun buildLoader(configPath: Path): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(configPath)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(
                        ObjectMapper.factoryBuilder()
                            .addDiscoverer(dataClassFieldDiscoverer())
                            .build()
                    )
                }
            }
            .let { loaderModifier?.invoke(it) ?: it }
            .build()
    }

    override fun finishLoading() {
        val node = rootNode.get(type)
        _typedConfig = node
        if (node == null)
            plugin.slF4JLogger.error("Config $file could not be loaded. Default values will be loaded", rootNode)
    }
}
