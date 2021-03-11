package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.enums.RunningState
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

@Serializable
class SGroup(private val groupID: Long) {
    var roselleServerIp = ""
    var serverIp = ""
    var isRepeat = false
    var welcomeMessage = ""
    var leaveMessage = ""
    
    val dailySignIns = ArrayList<Pair<Long, String>>()
    val autoApplyAcc = ArrayList<String>()
    val autoApplyFuz = ArrayList<String>()
}

object SSaveGroup: AutoSavePluginData("SGroupData") {
    var sGroupMap: MutableMap<Long, SGroup> by value(mutableMapOf())
    
    
    fun getSGroup(groupId: Long): SGroup {
        if(!sGroupMap.containsKey(groupId))
            sGroupMap[groupId] = SGroup(groupId)
        return sGroupMap[groupId]!!
    }
}

val sDataGroupMap = HashMap<Long, SDataGroup>()

data class SDataGroup(
    var runningState: RunningState = RunningState.FREE,
    var lastRunning: RunningState = RunningState.FREE,
    val hour24:IntArray = IntArray(5) { -1 },
    val ticTacToe: DataTicTacToe = DataTicTacToe()
) {
    
    companion object {
        fun getSDataGroup(groupId: Long): SDataGroup {
            if(!sDataGroupMap.containsKey(groupId))
                sDataGroupMap[groupId] = SDataGroup()
            return sDataGroupMap[groupId]!!
        }
    }
}

data class DataTicTacToe(
    val slot: IntArray = IntArray(10),
    var round: Int = 0
) {
    val player = Array<Member?>(3) { null }
}


fun Group.setRunningState(state: RunningState) {
    val sDataGroup = SDataGroup.getSDataGroup(id)
    sDataGroup.runningState = state
}

fun Group.getSGroup(): SGroup {
    val groupId = id
    
    if(!SSaveGroup.sGroupMap.containsKey(groupId))
        SSaveGroup.sGroupMap[groupId] = SGroup(groupId)
    return SSaveGroup.sGroupMap[groupId]!!
}