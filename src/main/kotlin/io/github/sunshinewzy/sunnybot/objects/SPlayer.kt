package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.objects.SSavePlayer.sPlayerMap
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
)

object SSavePlayer: AutoSavePluginData("SPlayerData") {
    var sPlayerMap: MutableMap<Long, SPlayer> by value(mutableMapOf())
    
    
    fun getSPlayer(id: Long): SPlayer {
        if(!sPlayerMap.containsKey(id))
            sPlayerMap[id] = SPlayer(id)
        return sPlayerMap[id]!!
    }
    
}


fun regPlayer(player: User) {
    val id = player.id
    
    if(!sPlayerMap.containsKey(id)){
        sPlayerMap[id] = SPlayer(id, 0)
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
    if(!sPlayerMap.containsKey(id))
        sPlayerMap[id] = SPlayer(id)
    return sPlayerMap[id]!!
}