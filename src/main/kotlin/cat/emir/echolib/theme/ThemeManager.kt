package cat.emir.echolib.theme

import cat.emir.echolib.EchoPlugin
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class ThemeManager private constructor(val tags: List<TagResolver>) {

    fun createTagResolver(): TagResolver {
        return TagResolver.resolver(tags)
    }

    companion object {
        private val emptyThemeManager = ThemeManager(emptyList())

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
        private val tags = mutableListOf<TagResolver>()

        /**
         * @param color hex formatted as "#FFFFFF" or "FFFFFF"
         */
        fun color(name: String, color: String) = apply {
            tags.add(TagResolver.resolver(name, Tag.styling {
                it.color(TextColor.color(color.removePrefix("#").toIntOrNull(16) ?: 0xFFFFFF))
            }))
        }

        /**
         * @param color hex formatted as 0xFFFFFF
         */
        fun color(name: String, color: Int) = apply {
            tags.add(TagResolver.resolver(name, Tag.styling { it.color(TextColor.color(color)) }))
        }

        fun unparsed(name: String, string: String) = apply {
            tags.add(Placeholder.unparsed(name, string))
        }

        fun parsed(name: String, string: String) = apply {
            tags.add(Placeholder.parsed(name, string))
        }

        fun component(name: String, component: ComponentLike) = apply {
            tags.add(Placeholder.component(name, component))
        }

        fun build(): ThemeManager {
            if (isInitialized) {
                throw IllegalStateException("ThemeManager already initialized, you cannot create a theme manager.")
            }
            internalInstance = ThemeManager(tags)
            return internalInstance
        }
    }
}