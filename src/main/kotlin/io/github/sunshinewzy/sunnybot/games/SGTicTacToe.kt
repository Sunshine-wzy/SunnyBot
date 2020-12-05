package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.utils.SLaTeX.laTeXImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import kotlin.random.Random


/**
 * 井字棋
 */
object SGTicTacToe : SGroupGame("井字棋", RunningState.TICTACTOE, RunningState.TICTACTOE_WAITING) {
    private val wrongFormatMsg = """
        您输入的消息格式不正确
        请重新输入 "#x,y" 以落子
        
        Tips:
        ① x为横坐标, y为纵坐标
        ② x和y均为1-3之间的数字
        ③ ,为英文逗号
    """.trimIndent()
    
    
    override suspend fun runGame(event: SGroupGameEvent) {
        val dataTicTacToe = event.sDataGroup.ticTacToe
        var str = event.msg
        if(str.startsWith("#")) str = str.substring(1)
        else return
        str = str.replace(" ", "")
        val group = event.group
        val member = event.member
        val id = member.id
        val player = dataTicTacToe.player
        val p1 = player[1] ?: return
        val p2 = player[2] ?: return
        
        var p = 0
        if(id == p1.id)
            p = 1
        else if(id == p2.id)
            p = 2
        
        if(p == 0){
            group.sendMsg(name,At(member) +
                PlainText("""
                    
                    您没有参加井字棋游戏。
                    当前玩家：
                    ① ${p1.nameCard} (${p1.id})
                    ② ${p2.nameCard} (${p2.id})
                """.trimIndent())
            )
            return
        }
        
        if(p != dataTicTacToe.round){
            group.sendMsg(name, At(member) +
                PlainText("\n当前是 ") + At(player[dataTicTacToe.round]!!) +
                PlainText(" " + """
                    的回合
                    您不能落子，请耐心等待！
                """.trimIndent())
            )
            return
        }
        
        if(str.length != 3 || str[0] !in '1'..'3' || str[1] != ',' || str[2] !in '1'..'3'){
            group.sendMsg(name, At(member) + PlainText(wrongFormatMsg))
            return
        }
        val args = str.split(',')
        if(args.size != 2){
            group.sendMsg(name, At(member) + PlainText(wrongFormatMsg))
            return
        }
        val x = args[0].toInt()
        val y = args[1].toInt()
        val order = coordToOrder(x, y)
        
        if(id == p1.id){
            if(!updateData(dataTicTacToe, 1, x, y, order)) {
                group.sendMsg(name, """
                    棋盘更新失败！
                    请确保下在棋盘内部
                    (1 ≤ x, y ≤ 3)
                    且勿重复落子
                """.trimIndent())
                return
            }
        }
        else if(id == p2.id){
            if(!updateData(dataTicTacToe, 2, x, y, order)) {
                group.sendMsg(name, """
                    棋盘更新失败！
                    请确保下在棋盘内部
                    (1 ≤ x, y ≤ 3)
                    且勿重复落子
                """.trimIndent())
                return
            }
        }
        group.sendMsg(name, group.laTeXImage(printBoard(dataTicTacToe)))
        
        if(judge(group, dataTicTacToe))
            return
        
        //回合更替
        if(dataTicTacToe.round == 1)
            dataTicTacToe.round = 2
        else if(dataTicTacToe.round == 2)
            dataTicTacToe.round = 1

        group.sendMsg(name, At(dataTicTacToe.player[dataTicTacToe.round]!!) +
            PlainText("\n到您的回合了，请输入 #x,y (x和y均为1-3之间的整数) 以落子")
        )
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        val group = event.group
        val member = event.member
        val sGroup = event.sGroup
        val dataTicTacToe = sDataGroupMap[group.id]!!.ticTacToe
        val sDataGroup = event.sDataGroup

        if(sDataGroup.runningState == RunningState.FREE) {
            dataTicTacToe.player[1] = member
            group.sendMsg(
                "井字棋",
                At(member) + PlainText(
                    "玩家1已就位\n" +
                        "等待第2位玩家输入 #井字棋 ..."
                )
            )
            sDataGroup.runningState = RunningState.TICTACTOE_WAITING
        } else if(sDataGroup.runningState == RunningState.TICTACTOE_WAITING) {
            val p1 = dataTicTacToe.player[1] ?: kotlin.run { 
                group.sendMsg(name, "玩家1对象不存在，玩家初始化失败！\n井字棋 游戏结束")
                group.setRunningState(RunningState.FREE)
                return
            }
            if(member.id == p1.id){
                group.sendMsg(name, At(member) +
                    PlainText(" 您不能自娱自乐！")
                )
                return
            }
            
            dataTicTacToe.player[2] = member
            if(dataTicTacToe.player[1] == null || dataTicTacToe.player[2] == null){
                group.sendMsg(name, "玩家初始化失败，游戏结束！")
                sDataGroup.runningState = RunningState.FREE
                return
            }

            sDataGroup.runningState = RunningState.TICTACTOE
            group.sendMsg(
                name,
                At(dataTicTacToe.player[1]!!) + PlainText(" ") +
                    At(dataTicTacToe.player[2]!!) + PlainText(
                    """
                        
                        玩家1、2均已就位
                        游戏开始！
                        请输入 "#x,y" 以落子
                        
                        Tips:
                        ① x为横坐标, y为纵坐标
                        ② x和y均为1-3之间的整数
                        ③ ,为英文逗号
                    """.trimIndent()
                )
            )
            
            val round = init(dataTicTacToe)
            group.sendMsg(name, group.laTeXImage(printBoard(dataTicTacToe)))
            group.sendMsg(name, At(dataTicTacToe.player[round]!!) +
                PlainText("\n您是先手，请输入 #x,y (x和y均为1-3之间的整数) 以落子")
            )
        }
    }

    /**
     * 胜利判定
     */
    private suspend fun judge(group: Group, dataTicTacToe: DataTicTacToe): Boolean {
        val slot = dataTicTacToe.slot
        //行
        for(i in 1..3){
            if(slot[i] != 0 && slot[i] == slot[i+3] && slot[i] == slot[i+6]){
                win(group, slot[i], dataTicTacToe)
                return true
            }
        }
        //列
        for(i in 1..7 step 3){
            if(slot[i] != 0 && slot[i] == slot[i+1] && slot[i] == slot[i+2]){
                win(group, slot[i], dataTicTacToe)
                return true
            }
        }
        //斜
        if(slot[1] != 0 && slot[1] == slot[5] && slot[1] == slot[9]){
            win(group, slot[1], dataTicTacToe)
            return true
        }
        if(slot[3] != 0 && slot[3] == slot[5] && slot[3] == slot[7]){
            win(group, slot[3], dataTicTacToe)
            return true
        }
        
        val p1 = dataTicTacToe.player[1] ?: return false
        val p2 = dataTicTacToe.player[2] ?: return false
        
        //平局判定
        var isDraw = true
        for(i in 1..9){
            if(slot[i] == 0){
                isDraw = false
                break
            }
        }
        if(isDraw){
            group.setRunningState(RunningState.FREE)
            val reward = Random.nextInt(3) + 3
            p1.addSTD(reward)
            p2.addSTD(reward)
            
            group.sendMsg(name, At(p1) + " 与 " + At(p2) +
                """

                    打成平局！
                    游戏奖励: $reward STD
                """.trimIndent()
            )
            return true
        }
        
        return false
    }
    
    private suspend fun win(group: Group, p: Int, dataTicTacToe: DataTicTacToe) {
        val winner = dataTicTacToe.player[p] ?: kotlin.run {
            sDataGroupMap[group.id]?.runningState = RunningState.FREE
            group.sendMsg(name, "玩家对象不存在，胜利判定失败！\n井字棋 游戏结束")
            return
        }

        group.setRunningState(RunningState.FREE)
        val reward = Random.nextInt(5) + 6
        winner.addSTD(reward)
        
        group.sendMsg(name, PlainText("恭喜玩家 ") + At(winner) +
            PlainText("""
                
                获得胜利！
                游戏奖励: $reward STD
            """.trimIndent())
        )
    }
    
    /**
     * <NPC落子>
     * 
     * 对抗搜索 极大极小搜索算法 alpha-beta剪枝
     */
    private fun npcChess(group: Group, dataTicTacToe: DataTicTacToe) {
        
        
    }
    
    private fun init(dataTicTacToe: DataTicTacToe): Int {
        for(i in 0..9) {
            dataTicTacToe.slot[i] = 0
        }
        
        val round = Random.nextInt(2) + 1
        dataTicTacToe.round = round
        return round
    }

    private fun printBoard(dataTicTacToe: DataTicTacToe): String {
        val args = Array(10) { " " }
        val slot = dataTicTacToe.slot
        for(i in 1..9) {
            if(slot[i] == 0)
                args[i] = "□"
            if(slot[i] == 1)
                args[i] = "×"
            else if(slot[i] == 2)
                args[i] = "■"
        }

        return """
            \begin{array}{1}
            y╲x & 1 & 2 & 3\\
            1 & ${args[1]} & ${args[2]} & ${args[3]}\\
            2 & ${args[4]} & ${args[5]} & ${args[6]}\\
            3 & ${args[7]} & ${args[8]} & ${args[9]}
            \end{array}
        """.trimIndent()
    }
    
    private fun coordToOrder(x: Int, y: Int): Int {
        return (y-1) * 3 + x
    }
    
    private fun updateData(dataTicTacToe: DataTicTacToe, p: Int, x: Int, y: Int, order: Int): Boolean {
        if(p !in 1..2 || x !in 1..3 || y !in 1..3 || order !in 1..9)
            return false
        //落子判重
        if(dataTicTacToe.slot[order] != 0)
            return false
        
        dataTicTacToe.slot[order] = p
        return true
    }
}