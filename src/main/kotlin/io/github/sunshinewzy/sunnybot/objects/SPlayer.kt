package io.github.sunshinewzy.sunnybot.objects

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.User

/**
 * @param id Íæ¼ÒQQºÅ
 * @param std Sunshine Technology Dollar
 */
@Serializable
data class SPlayer(
    private val id: Long,
    var std: Long = 0,
    var isDailySignIn: Boolean = false
) {
    val rconKeyMap = hashMapOf<String, String>()
    var selectedRconSymbol = ""
}

object SSavePlayer: AutoSavePluginData("SPlayerData") {
    val sPlayerMap: MutableMap<Long, SPlayer> by value(mutableMapOf())
    
    
    fun getSPlayer(id: Long): SPlayer {
        return sPlayerMap[id] ?: SPlayer(id).also { sPlayerMap[id] = it }
    }
}

fun User.addSTD(num: Long) {
    val sPlayer = SSavePlayer.getSPlayer(id)
    sPlayer.std += num
}

fun User.addSTD(num: Int) {
    addSTD(num.toLong())
}

fun User.getSPlayer(): SPlayer {
    return SSavePlayer.getSPlayer(id)
}