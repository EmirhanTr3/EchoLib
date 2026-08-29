package cat.emir.echolib.command.arguments

import cat.emir.echolib.lib.EchoLib
import cat.emir.echolib.extensions.toComponent
import cat.emir.echolib.lib.paper
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

fun CommandContext<CommandSourceStack>.getOfflinePlayer(name: String): OfflinePlayer {
    return this.getArgument(name, OfflinePlayer::class.java)
}

class CachedOfflinePlayerArgument(
    val suggestOffline: Boolean = false
) : CustomArgumentType.Converted<OfflinePlayer, String> {

    val ERROR_INVALID_PLAYER = SimpleCommandExceptionType(
        MessageComponentSerializer.message().serialize("<red>No player was found.</red>".toComponent())
    )

    override fun convert(nativeType: String): OfflinePlayer {
        return Bukkit.getServer().getOfflinePlayerIfCached(nativeType) ?: throw ERROR_INVALID_PLAYER.create()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val online = Bukkit.getOnlinePlayers().map { it.name }
        val offline = if (suggestOffline) EchoLib.instance.paper.offlinePlayerCache.suggestions(builder.remaining) else emptyList()

        (online + offline).distinct()
            .filter { it.startsWith(builder.remaining, true) }
            .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }
}