package cat.emir.echolib.extensions

import cat.emir.echolib.EchoPlugin
import cat.emir.echolib.theme.ThemeManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Parses a string to a component.
 * @param resolvers TagResolvers for MiniMessage
 * @return [Component]
 */
fun String.toComponent(vararg resolvers: TagResolver): Component {
    return EchoPlugin.miniMessage.deserialize(this,
        TagResolver.resolver(*resolvers, ThemeManager.instance.createTagResolver()))
}

/**
 * Parses a string list to a component list
 * @param resolvers TagResolvers for MiniMessage
 * @return [List] of [Component]
 */
fun String.toComponentList(vararg resolvers: TagResolver): List<Component> {
    val components = mutableListOf<Component>()
    val strings = this.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()

    for (string in strings) {
        components.add(EchoPlugin.miniMessage.deserialize(string,
            TagResolver.resolver(*resolvers, ThemeManager.instance.createTagResolver())))
    }

    return components
}