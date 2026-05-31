package cat.emir.echolib.theme

import cat.emir.echolib.EchoPlugin
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class ThemeManager private constructor(
    val plugin: EchoPlugin,
    val colors: Map<String, Int>
) {

    fun getTagResolver(name: String): TagResolver? {
        val color = colors[name] ?: return null
        return TagResolver.resolver(name, Tag.styling { it.color(TextColor.color(color)) })
    }

    fun createTagResolver(): TagResolver {
        return TagResolver.resolver(colors.map { (name, color) ->
            TagResolver.resolver(name, Tag.styling { it.color(TextColor.color(color)) })
        })
    }

    companion object {
        lateinit var instance: ThemeManager
            private set

        private val isInitialized: Boolean
            get() = ::instance.isInitialized

        fun builder(plugin: EchoPlugin) : Builder {
            if (::instance.isInitialized) {
                throw IllegalStateException("ThemeManager already initialized, you cannot create a theme manager.")
            }
            return Builder(plugin)
        }
    }

    class Builder(val plugin: EchoPlugin) {
        private val colors = mutableMapOf<String, Int>()

        /**
         * @param color hex formatted as "#FFFFFF" or "FFFFFF"
         */
        fun addColor(name: String, color: String) = apply {
            colors[name] = color.removePrefix("#").toIntOrNull(16) ?: 0xFFFFFF
        }

        /**
         * @param color hex formatted as 0xFFFFFF
         */
        fun addColor(name: String, color: Int) = apply {
            colors[name] = color
        }

        fun build(): ThemeManager {
            if (isInitialized) {
                throw IllegalStateException("ThemeManager already initialized, you cannot create a theme manager.")
            }
            instance = ThemeManager(plugin, colors)
            return instance
        }
    }
}