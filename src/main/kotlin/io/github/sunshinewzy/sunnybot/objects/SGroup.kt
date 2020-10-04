package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.commands.SCServerInfo
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

//val sGroupData = HashMap<Long, SGroup>()

@Serializable
class SGroup(private val groupID: Long) {
    var runningState = ""
    var hour24 = IntArray(5) { -1 }
    var serverIp: String = SCServerInfo.happylandIp

}

object SGroupData: AutoSavePluginData("SGroupData") {
    var sGroupMap: MutableMap<Long, SGroup> by value(mutableMapOf())
}