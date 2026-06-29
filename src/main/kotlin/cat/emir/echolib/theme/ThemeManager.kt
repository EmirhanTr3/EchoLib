package cat.emir.echolib.theme

import cat.emir.echolib.EchoPlugin
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.ServiceLoader

class ThemeManager private constructor(val colors: Map<String, Int>) {

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
        private val emptyThemeManager = ThemeManager(emptyMap())

        val instance: ThemeManager
            get() = if (isInitialized) internalInstance else emptyThemeManager

        private lateinit var internalInstance: ThemeManager

        private val isInitialized: Boolean
            get() = ::internalInstance.isInitialized

        fun builder(plugin: EchoPlugin) : Builder {
            if (::internalInstance.isInitialized) {
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
            internalInstance = ThemeManager(colors)
            return internalInstance
        }
    }
}