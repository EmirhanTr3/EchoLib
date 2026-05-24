package cat.emir.echolib.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun String.toComponent(vararg resolvers: TagResolver): Component {
    return MiniMessage.miniMessage().deserialize(this, *resolvers)
}

fun String.toComponentList(vararg resolvers: TagResolver): List<Component> {
    val components = mutableListOf<Component>()
    val strings = this.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()

    for (string in strings) {
        components.add(MiniMessage.miniMessage().deserialize(string, *resolvers))
    }

    return components
}