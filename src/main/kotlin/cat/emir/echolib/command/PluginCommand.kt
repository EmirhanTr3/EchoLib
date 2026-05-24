package cat.emir.echolib.command

import cat.emir.echolib.EchoPlugin
import cat.emir.echolib.command.CommandLib.CommandAction
import cat.emir.echolib.command.CommandLib.CommandBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

/**
 * returns the executor player if present
 */
fun CommandContext<CommandSourceStack>.getPlayer(): Player? {
    if (this.source.sender is Player) return this.source.sender as Player
    this.source.sender.sendRichMessage("<red>You cannot run this command as console.</red>")
    return null
}

/**
 * returns the player from a player argument with provided name
 */
fun CommandContext<CommandSourceStack>.getPlayer(argument: String): Player? {
    val player = this.getArgument(argument, PlayerSelectorArgumentResolver::class.java).resolve(this.source).firstOrNull()
    if (player == null) {
        this.source.sender.sendRichMessage("<red>No player was found</red>")
        return null
    }
    return player
}

fun CommandContext<CommandSourceStack>.getPlayers(argument: String): List<Player>? {
    val players = this.getArgument(argument, PlayerSelectorArgumentResolver::class.java).resolve(this.getSource())
    if (players.isEmpty()) {
        this.source.sender.sendRichMessage("<red>No player was found</red>")
        return null
    }
    return players
}

abstract class PluginCommand<T: EchoPlugin>(protected val plugin: T) {
    open val aliases = setOf<String>()
    
    fun command(
        name: String,
        setup: CommandBuilder.(LiteralArgumentBuilder<CommandSourceStack>) -> Unit
    ): LiteralArgumentBuilder<CommandSourceStack> {
        val builder = CommandBuilder(name)
        builder.setup(builder.node)
        return builder.node
    }

    /**
     * for java compatibility
     */
    fun command(name: String, setup: CommandAction<CommandBuilder>): LiteralArgumentBuilder<CommandSourceStack> {
        val builder = CommandBuilder(name)
        setup.accept(builder)
        return builder.node
    }

    open fun meetsRequirements(): Boolean = true

    abstract fun getCommand(): LiteralArgumentBuilder<CommandSourceStack>

}