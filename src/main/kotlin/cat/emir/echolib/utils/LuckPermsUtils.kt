package cat.emir.echolib.utils

import cat.emir.echolib.plugin.EchoPlugin
import net.luckperms.api.LuckPerms
import net.luckperms.api.model.data.DataMutateResult
import org.bukkit.entity.Player

import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.query.Flag
import net.luckperms.api.query.QueryOptions
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

class LuckPermsUtils(val plugin: EchoPlugin, val luckPerms: LuckPerms) {
    val offline = OfflineLuckPermsUtils(plugin, luckPerms)

    fun getGroup(name: String): Group? {
        return luckPerms.groupManager.getGroup(name)
    }

    fun getAllGroups(): Set<Group> {
        return luckPerms.groupManager.loadedGroups.toSet()
    }

    fun getUser(player: Player): User? {
        return luckPerms.userManager.getUser(player.uniqueId)
    }

    fun getPrefix(player: Player): String {
        return getUser(player)?.cachedData?.metaData?.prefix ?: ""
    }

    fun getSuffix(player: Player): String {
        return getUser(player)?.cachedData?.metaData?.suffix ?: ""
    }

    fun getPlayerGroups(player: Player): Set<Group> {
        val queryOptions = QueryOptions.defaultContextualOptions().toBuilder()
            .flag(Flag.RESOLVE_INHERITANCE, false)
            .build()
        return getUser(player)?.getInheritedGroups(queryOptions)?.toSet() ?: emptySet()
    }

    fun addGroup(player: Player, group: Group): Boolean {
        val user = getUser(player) ?: return false
        val node = InheritanceNode.builder(group)
            .value(true)
            .build()

        val result = user.data().add(node)
        if (result != DataMutateResult.SUCCESS) return false

        luckPerms.userManager.saveUser(user)
        return true
    }

    fun removeGroup(player: Player, group: Group): Boolean {
        val user = getUser(player) ?: return false
        val node = InheritanceNode.builder(group)
            .value(true)
            .build()

        val result = user.data().remove(node)
        if (result != DataMutateResult.SUCCESS) return false

        luckPerms.userManager.saveUser(user)
        return true
    }

    class OfflineLuckPermsUtils(val plugin: EchoPlugin, val luckPerms: LuckPerms) {
        fun getUser(offlinePlayer: OfflinePlayer): CompletableFuture<User> {
            return luckPerms.userManager.loadUser(offlinePlayer.uniqueId)
        }

        fun getPrefix(offlinePlayer: OfflinePlayer): CompletableFuture<String> {
            return getUser(offlinePlayer).thenApplyAsync { user ->
                user.cachedData.metaData.prefix ?: ""
            }
        }

        fun getSuffix(player: Player): CompletableFuture<String> {
            return getUser(player).thenApplyAsync { user ->
                user.cachedData.metaData.suffix ?: ""
            }
        }

        fun getPlayerGroups(offlinePlayer: OfflinePlayer): CompletableFuture<Set<Group>> {
            return getUser(offlinePlayer).thenApplyAsync { user ->
                val queryOptions = QueryOptions.defaultContextualOptions().toBuilder()
                    .flag(Flag.RESOLVE_INHERITANCE, false)
                    .build()

                user.getInheritedGroups(queryOptions).toSet()
            }
        }

        fun addGroup(offlinePlayer: OfflinePlayer, group: Group): CompletableFuture<Boolean> {
            return getUser(offlinePlayer).thenApplyAsync {
                val node = InheritanceNode.builder(group)
                    .value(true)
                    .build()

                val result = it.data().add(node)
                if (result != DataMutateResult.SUCCESS) return@thenApplyAsync false

                luckPerms.userManager.saveUser(it)
                return@thenApplyAsync true
            }

        }

        fun removeGroup(offlinePlayer: OfflinePlayer, group: Group): CompletableFuture<Boolean> {
            return getUser(offlinePlayer).thenApplyAsync {
                val node = InheritanceNode.builder(group)
                    .value(true)
                    .build()

                val result = it.data().remove(node)
                if (result != DataMutateResult.SUCCESS) return@thenApplyAsync false

                luckPerms.userManager.saveUser(it)
                return@thenApplyAsync true
            }
        }
    }
}