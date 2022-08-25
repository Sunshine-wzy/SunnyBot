package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.objects.data.ImageLibraryData
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object SunnyData : AutoSavePluginData("SunnyData") {
    val rcon: MutableMap<String, RconData> by value()
    val image: MutableMap<String, ImageLibraryData> by value()
    
}

@Serializable
class RconData(
    val owner: Long,
    val ip: String,
    val password: String
) {
    val operators = hashSetOf<Long>()
    
    
    fun checkExecutor(id: Long): Executor =
        if(owner == id) Executor.OWNER
        else if(operators.contains(id)) Executor.OPERATOR
        else Executor.DEFAULT
    
    
    companion object {
        fun buildKey(owner: Long, ip: String): String = "$owner@$ip"
    }
    
    enum class Executor {
        OWNER,
        OPERATOR,
        DEFAULT
    }
}

