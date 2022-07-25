package io.github.sunshinewzy.sunnybot.objects

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object SunnyData: AutoSavePluginData("SunnyData") {
    val rcon: MutableMap<String, RconData> by value()
    
}

@Serializable
class RconData(
    val owner: Long,
    val ip: String,
    val password: String
) {
    val operators = hashSetOf<Long>()
    
    
    companion object {
        fun buildKey(owner: Long, ip: String): String = "$owner@$ip"
    }
}