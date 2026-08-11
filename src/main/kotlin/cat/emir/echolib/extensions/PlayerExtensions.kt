package cat.emir.echolib.extensions

import org.bukkit.OfflinePlayer

val OfflinePlayer.nameOrUniqueId
    get() = this.name ?: this.uniqueId.toString()