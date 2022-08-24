package io.github.sunshinewzy.sunnybot.objects

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.enums.ServerType
import io.github.sunshinewzy.sunnybot.games.game.SGChess
import io.github.sunshinewzy.sunnybot.games.game.SGFiveInARow
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.random.Random

@Serializable
class SGroup(private val groupID: Long) {
    var serverIp = ServerType.NOT to ""
    
    var isRepeat = false
    var isOpen = true
    var isGaoKaoCountDown = false
    
    var welcomeMessage = ""
    var leaveMessage = ""
    
    val dailySignIns = ArrayList<Pair<Long, String>>()
    val autoApply = ArrayList<String>()
    val autoReject = ArrayList<String>()
    val serverIps = HashMap<String, Pair<ServerType, String>>()
    val reminders = ArrayList<DataReminder>()
}

object SSaveGroup: AutoSavePluginData("SGroupData") {
    val sGroupMap: MutableMap<Long, SGroup> by value(mutableMapOf())
    
    
    fun getSGroup(groupId: Long): SGroup {
        return sGroupMap.getOrPut(groupId) { SGroup(groupId) }
    }
}


data class SDataGroup(
    var runningState: RunningState = RunningState.FREE,
    var lastRunningState: RunningState = RunningState.FREE,
    val players: IdTimeContainer = IdTimeContainer(60_000L),
    val hour24: IntArray = IntArray(5) { -1 },
    val ticTacToe: DataTicTacToe = DataTicTacToe(),
    val chess: DataChess = DataChess(),
    val fiveInARow: DataFiveInARow = DataFiveInARow()
) {
    
    companion object {
        val sDataGroupMap = HashMap<Long, SDataGroup>()
        
        
        fun getSDataGroup(groupId: Long): SDataGroup {
            return sDataGroupMap.getOrPut(groupId) { SDataGroup() }
        }
    }
}

data class DataTicTacToe(
    val slot: IntArray = IntArray(10),
    var round: Int = 0
) {
    val players = Array<Member?>(3) { null }
}

data class DataChess(
    var board: SGChess.ChessBoard? = null
) {
    val players: Array<Member?> = Array(3) { null }
    

    fun init() {
        board = SGChess.ChessBoard()
        
        if(Random.nextInt(1, 3) == 2){
            val temp = players[1]
            players[1] = players[2]
            players[2] = temp
        }
    }
}

data class DataFiveInARow(
    var board: SGFiveInARow.ChessBoard? = null
) {
    val players: Array<Member?> = Array(3) { null }


    fun init() {
        board = SGFiveInARow.ChessBoard()

        if(Random.nextInt(1, 3) == 2){
            val temp = players[1]
            players[1] = players[2]
            players[2] = temp
        }
    }
}

@Serializable
data class DataReminder(
    var hour: Int,
    var minute: Int,
    var content: String,
    var isOnce: Boolean = false,
    var isAtAll: Boolean = false
) {
    private fun timeToString(): String {
        val str = StringBuilder(10)
        if(hour < 10) str.append("0")
        str.append("$hour:")
        if(minute < 10) str.append("0")
        str.append(minute)
        return str.toString()
    }
    
    private fun statusToString(): String {
        var str = ""
        if(isOnce) str += "[仅一次] "
        if(isAtAll) str += "[At全体成员] "
        return str
    }
    
    override fun toString(): String = "${timeToString()} ${statusToString()}-> $content"
}


fun Group.setRunningState(state: RunningState) {
    val sDataGroup = SDataGroup.getSDataGroup(id)
    sDataGroup.runningState = state
    
    if(state == RunningState.FREE) {
        sDataGroup.players.clear()
    }
}

fun Group.getSGroup(): SGroup =
    SSaveGroup.getSGroup(id)

fun Group.getSData(): SDataGroup =
    SDataGroup.getSDataGroup(id)