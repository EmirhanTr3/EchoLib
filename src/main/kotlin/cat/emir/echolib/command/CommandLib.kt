package cat.emir.echolib.command

import cat.emir.echolib.EchoPlugin
import cat.emir.echolib.utils.ClassUtils
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import java.util.concurrent.CompletableFuture

class CommandLib {

    companion object {
        /**
         * Commands extending [PluginSubCommand] will not be registered automatically, use their [PluginSubCommand.getCommand] function inside another command instead to use them.
         */
        fun <T : EchoPlugin> registerCommands(plugin: T, pkg: String) {
            plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { manager ->
                val registrar = manager.registrar()
                ClassUtils.findClasses(
                    plugin = plugin,
                    pkg = pkg,
                    condition = { it.extendsSuperclass(PluginCommand::class.java) && !it.extendsSuperclass(PluginSubCommand::class.java) },
                    function = {
                        val command = it.loadClass().asSubclass(PluginCommand::class.java).constructors[0].newInstance(plugin) as PluginCommand<*>
                        if (command.meetsRequirements()) {
                            registrar.register(command.getCommand().build(), command.aliases)
                            plugin.logger.info("[CommandLib] [${it.simpleName}] Registered command ${command.getCommand().literal}" +
                                    if (!command.aliases.isEmpty())
                                        " with aliases ${command.aliases.joinToString(", ")}"
                                    else ""
                            )
                        } else {
                            plugin.logger.info("[CommandLib] [${it.simpleName}] Skipped command ${command.getCommand().literal} due to not meeting requirements.")
                        }
                    }
                )
            }
        }
    }

    fun interface CommandAction<T> {
        fun accept(builder: T)
    }

    interface CommandNode {
        val node: com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, *>

        fun requires(block: (CommandSourceStack) -> Boolean) {
            node.requires(block)
        }

        fun executes(block: (CommandContext<CommandSourceStack>) -> Int) {
            node.executes(block)
        }

        fun <T> argument(
            name: String,
            type: ArgumentType<T>,
            setup: ArgumentBuilder<T>.(RequiredArgumentBuilder<CommandSourceStack, T>) -> Unit
        ) {
            val argumentBuilder = ArgumentBuilder(name, type)
            argumentBuilder.setup(argumentBuilder.node)
            node.then(argumentBuilder.node)
        }

        /**
         * for java compatibility
         */
        fun <T> argument(
            name: String,
            type: ArgumentType<T>,
            setup: CommandAction<ArgumentBuilder<T>>
        ) {
            val argumentBuilder = ArgumentBuilder(name, type)
            setup.accept(argumentBuilder)
            node.then(argumentBuilder.node)
        }

        fun subcommand(name: String, setup: CommandBuilder.(LiteralArgumentBuilder<CommandSourceStack>) -> Unit) {
            val commandBuilder = CommandBuilder(name)
            commandBuilder.setup(commandBuilder.node)
            node.then(commandBuilder.node)
        }

        fun subcommand(builder: LiteralArgumentBuilder<CommandSourceStack>) {
            node.then(builder)
        }

        /**
         * for java compatibility
         */
        fun subcommand(name: String, setup: CommandAction<CommandBuilder>) {
            val commandBuilder = CommandBuilder(name)
            setup.accept(commandBuilder)
            node.then(commandBuilder.node)
        }
    }

    class CommandBuilder(name: String) : CommandNode {
        override val node: LiteralArgumentBuilder<CommandSourceStack> = LiteralArgumentBuilder.literal(name)
    }

    class ArgumentBuilder<T>(name: String, type: ArgumentType<T>) : CommandNode {
        override val node: RequiredArgumentBuilder<CommandSourceStack, T> = RequiredArgumentBuilder.argument(name, type)

        fun suggests(block: (CommandContext<CommandSourceStack>, SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
            node.suggests(block)
        }
    }
}