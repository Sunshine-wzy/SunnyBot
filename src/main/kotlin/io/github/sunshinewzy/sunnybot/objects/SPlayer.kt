package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Member

/**
 * @param id Íæ¼ÒQQºÅ
 * @param std Sunshine Technology Dollar
 */
@Serializable
class SPlayer(private val id: Long, var std: Long = 0) {
    
}

object SPlayerData: AutoSavePluginData("SPlayerData") {
    var sPlayerMap: MutableMap<Long, SPlayer> by value(mutableMapOf())
}


fun regPlayer(member: Member) {
    val id = member.id
    
    if(!sPlayerMap.containsKey(id)){
        sPlayerMap[id] = SPlayer(id, 0)
    }
}