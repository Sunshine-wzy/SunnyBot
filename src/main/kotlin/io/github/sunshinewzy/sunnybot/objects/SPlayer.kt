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
data class SPlayer(private val id: Long, var std: Long = 0)

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

fun User.addPlayerSTD(num: Long) {
    val sPlayer = SSavePlayer.getSPlayer(id)
    sPlayer.std += num
}

fun User.addPlayerSTD(num: Int) {
    addPlayerSTD(num.toLong())
}