package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.DataTicTacToe
import io.github.sunshinewzy.sunnybot.objects.sDataGroup
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.setDigit
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

/**
 * 井字棋
 */
object SGTicTacToe : SGroupGame("井字棋") {
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
        if(dataTicTacToe.p1 == null || dataTicTacToe.p2 == null)
            return
        val p1 = dataTicTacToe.p1!!
        val p2 = dataTicTacToe.p2!!
        
        if(id != p1.id && id != p2.id){
            group.sendMessage(At(member) +
                PlainText("""
                    您没有参加井字棋游戏。
                    当前玩家：
                    ① ${p1.nameCard} (${p1.id})
                    ② ${p2.nameCard} (${p2.id})
                """.trimIndent())
            )
            return
        }
        
        if(str.length != 3 || str[0] !in '1'..'3' || str[1] != ',' || str[2] !in '1'..'3'){
            group.sendMessage(At(member) + PlainText(wrongFormatMsg))
            return
        }
        val args = str.split(',')
        if(args.size != 2){
            group.sendMessage(At(member) + PlainText(wrongFormatMsg))
            return
        }
        val x = args[0].toInt()
        val y = args[1].toInt()
        val order = coordToOrder(x, y)
        
        if(id == p1.id){
            updateData(dataTicTacToe, 1, x, y, order)
        }
        else if(id == p2.id){
            updateData(dataTicTacToe, 2, x, y, order)
        }
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        val group = event.group
        val member = event.member
        val sGroup = event.sGroup
        val dataTicTacToe = sDataGroup[group.id]!!.ticTacToe

        if(sGroup.runningState == "") {
            dataTicTacToe.p1 = member
            group.sendMsg(
                "井字棋",
                At(member) + PlainText(
                    "玩家1已就位\n" +
                        "等待第2个玩家输入 #井字棋 ..."
                )
            )
            sGroup.runningState = "${name}1"
        } else if(sGroup.runningState == "${name}1") {
            dataTicTacToe.p2 = member
            if(dataTicTacToe.p1 == null || dataTicTacToe.p2 == null){
                group.sendMsg(name, "玩家初始化失败，游戏结束！")
                sGroup.runningState = ""
                return
            }
            
            sGroup.runningState = name
            group.sendMsg(
                name,
                At(dataTicTacToe.p1!!) + PlainText(" ") +
                    At(dataTicTacToe.p2!!) + PlainText(
                    """
                        玩家1、2均已就位
                        游戏开始！
                        请输入 "#x,y" 以落子
                        
                        Tips:
                        ① x为横坐标, y为纵坐标
                        ② x和y均为1-3之间的数字
                        ③ ,为英文逗号
                    """.trimIndent()
                )
            )
            
            init(dataTicTacToe)
            group.sendMsg(name, printBoard(dataTicTacToe))
        }
    }

    private fun init(dataTicTacToe: DataTicTacToe) {
        for(i in 0..9) {
            dataTicTacToe.slot[i] = 0
        }
        for(i in 0..3) {
            dataTicTacToe.line[i] = 0
            dataTicTacToe.row[i] = 0
        }
        dataTicTacToe.ldil = 0
        dataTicTacToe.rdil = 0
    }

    private fun printBoard(dataTicTacToe: DataTicTacToe): String {
        val args = Array(10) { " " }
        val slot = dataTicTacToe.slot
        for(i in 1..9) {
            if(slot[i] == 1)
                args[i] = "×"
            else if(slot[i] == 2)
                args[i] = "O"
        }

        return """
          ╲ x 1\t2\t3
          y ┌───┬───┬───┐
          1 │\t${args[1]}\t│\t${args[2]}\t│\t${args[3]}\t│
          - ├───┼───┼───┤
          2 │\t${args[4]}\t│\t${args[5]}\t│\t${args[6]}\t│
          - ├───┼───┼───┤
          3 │\t${args[7]}\t│\t${args[8]}\t│\t${args[9]}\t│
          - └───┴───┴───┘
        """.trimIndent()
    }
    
    private fun coordToOrder(x: Int, y: Int): Int {
        return (y-1) * 3 + x
    }
    
    private fun updateData(dataTicTacToe: DataTicTacToe, p: Int, x: Int, y: Int, order: Int) {
        dataTicTacToe.slot[order] = p
//        dataTicTacToe.line[y] = dataTicTacToe.line[y].setDigit(x, p)
//        dataTicTacToe.row[x] = dataTicTacToe.row[x].setDigit(y, p)
        if(order == 1 || order == 5 || order == 9)
            dataTicTacToe.ldil = p
        if(order == 3 || order == 5 || order == 7)
            dataTicTacToe.rdil = p
    }
}