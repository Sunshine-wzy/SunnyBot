package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.commands.SCServerInfo
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Member

@Serializable
class SGroup(private val groupID: Long) {
    var runningState = ""
    var hour24 = IntArray(5) { -1 }
    var serverIp: String = SCServerInfo.happylandIp
    
}

object SGroupData: AutoSavePluginData("SGroupData") {
    var sGroupMap: MutableMap<Long, SGroup> by value(mutableMapOf())
}


val sDataGroup = HashMap<Long, SDataGroup>()

data class SDataGroup(val ticTacToe: DataTicTacToe = DataTicTacToe())

data class DataTicTacToe(
    var slot: IntArray = IntArray(10),
    var line: IntArray = IntArray(4),
    var row: IntArray = IntArray(4),
    var ldil: Int = 0,
    var rdil: Int = 0
) {
    var p1: Member? = null
    var p2: Member? = null
}