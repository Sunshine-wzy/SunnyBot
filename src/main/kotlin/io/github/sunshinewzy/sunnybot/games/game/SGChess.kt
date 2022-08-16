package io.github.sunshinewzy.sunnybot.games.game

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.games.SGroupGame
import io.github.sunshinewzy.sunnybot.games.game.SGChess.ChessType.*
import io.github.sunshinewzy.sunnybot.objects.setRunningState
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.toInputStream
import io.github.sunshinewzy.sunnybot.utils.PII
import io.github.sunshinewzy.sunnybot.utils.PIIArrayList
import io.github.sunshinewzy.sunnybot.utils.SImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.image.BufferedImage

object SGChess : SGroupGame("围棋", RunningState.CHESS) {
    private val imgChessBoard = SImage.loadImage("Chess/ChessBoard.png")
    private val imgBlackPieces = SImage.loadImage("Chess/BlackPieces.png")
    private val imgWhitePieces = SImage.loadImage("Chess/WhitePieces.png")
    private val wrongFormatMsg = """
        您发送的指令格式不正确
        请重新发送形如 "#A1" 的指令以落子
        
        Tips:
        ① A 处可以为 A-S 之间的任意一个字母
        ② 1 处可以为 1-19 之间的任意一个整数
        ③ 可以有空格，不区分大小写
    """.trimIndent()
    
    
    override suspend fun runGame(event: SGroupGameEvent) {
        val dataChess = event.sDataGroup.chess
        var str = event.msg
        if(str.startsWith("#")) str = str.substring(1)
        else return
        str = str.replace(" ", "").toUpperCase()
        val group = event.group
        val member = event.member
        val id = member.id
        val player = dataChess.players
        val p1 = player[1] ?: return
        val p2 = player[2] ?: return
        val board = dataChess.board ?: return
        
        var p = 0
        if(id == p1.id)
            p = 1
        else if(id == p2.id)
            p = 2

        if(p == 0){
            group.sendMsg(
                name,At(member) +
                PlainText("""
                    
                    您没有参加围棋对弈。
                    当前玩家：
                    ① ${p1.nameCardOrNick} (${p1.id})
                    ② ${p2.nameCardOrNick} (${p2.id})
                """.trimIndent())
            )
            return
        }

        if(p != board.round.id){
            group.sendMsg(
                name, At(member) +
                PlainText("\n当前是\n") + At(player[board.round.id]!!) +
                PlainText("""
                    
                    的回合
                    您不能落子，请耐心等待！
                """.trimIndent())
            )
            return
        }

        if(
            str.length !in 2..3 || str[0] !in 'A'..'S' ||
            if(str.length == 3) !(str[1] == '1' && str[2] in '0'..'9')
            else str[1] !in '1'..'9'
        ){
            group.sendMsg(name, At(member) + wrongFormatMsg)
            return
        }
        
        val x = str[0] - 'A' + 1
        val y =
            if(str.length == 2) str[1] - '0'
            else 10 + (str[2] - '0')
        val type = if(id == p1.id) BLACK else WHITE

        //落子合法性检验
        if(!board.placePieces(type, x, y)) {
            group.sendMsg(
                name, """
                    棋盘更新失败！
                    请确保输入的坐标位置能落子
                """.trimIndent())
            return
        }

        if(board.judge(group, x, y))
            return
        
        val image = board.printBoard().toInputStream()?.uploadAsImage(group) ?: return
        group.sendMsg(name, image)
        
        //回合更替
        if(board.round == BLACK)
            board.round = WHITE
        else if(board.round == WHITE)
            board.round = BLACK

        group.sendMsg(
            name, At(dataChess.players[board.round.id]!!) +
            PlainText("\n您执${if(board.round.id == 1) "黑子" else "白子"}，到您的回合了，请发送形如 \"#A1\" 的指令以落子")
        )
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        event.apply {
            val dataChess = sDataGroup.chess
            
            if(sDataGroup.runningState == RunningState.FREE) {
                dataChess.players[1] = member
                group.sendMsg(name,
                    At(member) + PlainText(
                        "玩家1已就位\n" +
                            "等待第2位玩家输入 #围棋"
                    )
                )
                sDataGroup.runningState = RunningState.CHESS_WAITING
            } else if(sDataGroup.runningState == RunningState.CHESS_WAITING) {
                val p1 = dataChess.players[1] ?: kotlin.run {
                    group.sendMsg(name, "玩家1对象不存在，玩家初始化失败！\n围棋 游戏结束")
                    group.setRunningState(RunningState.FREE)
                    return
                }
                if(member.id == p1.id){
                    group.sendMsg(
                        name, At(member) +
                        PlainText(" 您不能自娱自乐！")
                    )
                    return
                }

                dataChess.players[2] = member
                if(dataChess.players[1] == null || dataChess.players[2] == null){
                    group.sendMsg(name, "玩家初始化失败，游戏结束！")
                    group.setRunningState(RunningState.FREE)
                    return
                }

                sDataGroup.runningState = RunningState.CHESS
                with(sDataGroup.players) {
                    clear()
                    dataChess.players[1]?.id?.let { add(it) }
                    dataChess.players[2]?.id?.let { add(it) }
                }
                
                group.sendMsg(
                    name,
                    At(dataChess.players[1]!!) + PlainText(" ") +
                        At(dataChess.players[2]!!) + PlainText(
                        """
                        
                        玩家1、2均已就位
                        游戏开始！
                        请发送形如 "#A1" 的指令以落子
                        
                        Tips:
                        ① A 处可以为 A-S 之间的任意一个字母
                        ② 1 处可以为 1-19 之间的任意一个整数
                        ③ 可以有空格，不区分大小写
                    """.trimIndent()
                    )
                )
                
                dataChess.init()
                val image = dataChess.board?.printBoard()?.toInputStream()?.uploadAsImage(group) ?: return
                group.sendMsg(name, image)
                group.sendMsg(
                    name, At(dataChess.players[1]!!) +
                    PlainText("\n您执黑子，请发送形如 \"#A1\" 的指令以落子")
                )
            }
        }
    }
    
    
    class ChessBoard {
        private val slots = Array(20) {
            Array(20) { EMPTY }
        }
        
        var round = BLACK
        val eatCount = EatCount()
        val manual = ArrayList<String>()        //棋谱记录
        
        
        fun update() {
            
        }

        /**
         * 落子时触发判断
         */
        suspend fun judge(group: Group, x: Int, y: Int): Boolean {
            val list = PIIArrayList()
            val eat = EatCount()
            
            //判断是否为活棋
            if(isInBoard(x + 1, y) && !dfsLiberty(list, x + 1, y, slots[x + 1][y]))
                clearPieces(list, eat)
            
            list.clear()
            if(isInBoard(x - 1, y) && !dfsLiberty(list, x - 1, y, slots[x - 1][y]))
                clearPieces(list, eat)

            list.clear()
            if(isInBoard(x, y + 1) && !dfsLiberty(list, x, y + 1, slots[x][y + 1]))
                clearPieces(list, eat)

            list.clear()
            if(isInBoard(x, y - 1) && !dfsLiberty(list, x, y - 1, slots[x][y - 1]))
                clearPieces(list, eat)
            
            if(!eat.isEmpty()){
                eatCount += eat

                group.sendMsg(name, """
                黑方本次吃子: ${eat.black}
                黑方累计吃子: ${eatCount.black}
                
                白方本次吃子: ${eat.white}
                白方累计吃子: ${eatCount.white}
            """.trimIndent())
            }
            
            return false
        }
        
        private fun clearPieces(list: PIIArrayList, eatCount: EatCount, except: PII = -1 to -1) {
            list.forEach { 
                if(it == except)
                    return@forEach
                
                when(slots[it.first][it.second]){
                    BLACK -> eatCount.white++
                    
                    WHITE -> eatCount.black++
                    
                    else -> {}
                }
                    
                
                slots[it.first][it.second] = EMPTY
            }
        }
        
        private fun clearPieces(list: PIIArrayList, eatCount: EatCount, exceptX: Int, exceptY: Int) {
            clearPieces(list, eatCount, exceptX to exceptY)
        }

        /**
         * 深度优先搜索 - 判断是否有气
         */
        private fun dfsLiberty(list: PIIArrayList, x: Int, y: Int, type: ChessType): Boolean {
            if(isInBoard(x, y)){
                val pair = x to y
                if(list.contains(pair))
                    return false
                
                when(slots[x][y]){
                    EMPTY -> return true

                    type -> {
                        list += pair
                        
                        return dfsLiberty(list, x + 1, y, type) || dfsLiberty(list, x - 1, y, type)
                            || dfsLiberty(list, x, y + 1, type) || dfsLiberty(list, x, y - 1, type)
                    }
                    
                    else -> {}
                }
            }

            return false
        }
        
        fun printBoard(): BufferedImage {
            val img = BufferedImage(imgChessBoard.width, imgChessBoard.height, BufferedImage.TYPE_4BYTE_ABGR)
            val graph = img.createGraphics()
            
            graph.drawImage(imgChessBoard, 0, 0, null)
            
            slots.forEachIndexed { x, arr ->
                arr.forEachIndexed type@{ y, type ->
                    when(type) {
                        EMPTY -> return@type
                        
                        BLACK -> {
                            graph.drawImage(imgBlackPieces, X0 + DX * (x - 1), Y0 - DY * (y - 1), null)
                        }
                        
                        WHITE -> {
                            graph.drawImage(imgWhitePieces, X0 + DX * (x - 1), Y0 - DY * (y - 1), null)
                        }
                    }
                }
            }
            
            return img
        }

        fun placePieces(type: ChessType, x: Int, y: Int): Boolean {
            if(!isInBoard(x, y))
                return false
            
            if(slots[x][y] != EMPTY)
                return false
            
            //己方真眼特判
            if(isRealEye(type, x, y))
                return false
            
            val oppoType = type.getOpposite()
            //敌方特判
            when(aroundJudge(oppoType, x, y)) {
                4 to 0, 3 to 1, 2 to 2 -> {
                    val pair = x to y
                    val listAll = PIIArrayList()
                    
                    if(dfsLiberty(listAll, x + 1, y, oppoType)){
                        val list2 = PIIArrayList()
                        list2 += pair
                        
                        if(listAll.contains(x - 1 to y) || dfsLiberty(list2, x - 1, y , oppoType)){
                            listAll += list2
                            val list3 = PIIArrayList()
                            list3 += pair
                            
                            if(listAll.contains(x to y + 1) || dfsLiberty(list3, x, y + 1, oppoType)){
                                listAll += list3
                                val list4 = PIIArrayList()
                                list4 += pair
                                
                                if(listAll.contains(x to y - 1) || dfsLiberty(list4, x, y - 1, oppoType)){
                                    
                                    return false
                                }
                            }
                        }
                    }
                    
                }
            }
            
            //棋谱记录
//            manual += "${if(type == BLACK) "黑" else "白"},$x,$y"

            slots[x][y] = type
            return true
        }
        
        fun end() {
            
        }
        
        
        private fun isRealEye(type: ChessType, x: Int, y: Int): Boolean {
            if(!isInBoard(x, y))
                return false
            
            if(slots[x][y] != EMPTY)
                return false
            
            when(aroundJudge(type, x, y)) {
                //中间
                4 to 0 -> {
                    if(angleJudge(type, x, y).first >= 3)
                        return true
                }
                
                //边
                3 to 1 -> {
                    if(angleJudge(type, x, y).first >= 2)
                        return true
                }
                
                //角
                2 to 2 -> {
                    if(angleJudge(type, x, y).first >= 1)
                        return true
                }
            }
            
            return false
        }

        private fun aroundJudge(type: ChessType, x: Int, y: Int): Pair<Int, Int> {
            var cnt = 0
            var outOfBoard = 0

            if(isInBoard(x + 1, y)){
                if(slots[x + 1][y] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x - 1, y)){
                if(slots[x - 1][y] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x, y + 1)){
                if(slots[x][y + 1] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x, y - 1)){
                if(slots[x][y - 1] == type)
                    cnt++
            }
            else outOfBoard++


            return cnt to outOfBoard
        }
        
        private fun angleJudge(type: ChessType, x: Int, y: Int): Pair<Int, Int> {
            var cnt = 0
            var outOfBoard = 0
            
            if(isInBoard(x + 1, y + 1)){
                if(slots[x + 1][y + 1] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x + 1, y - 1)){
                if(slots[x + 1][y - 1] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x - 1, y + 1)){
                if(slots[x - 1][y + 1] == type)
                    cnt++
            }
            else outOfBoard++

            if(isInBoard(x - 1, y - 1)){
                if(slots[x - 1][y - 1] == type)
                    cnt++
            }
            else outOfBoard++
            
            
            return cnt to outOfBoard
        }
        
        
        companion object {
            private const val X0 = 103 - 50
            private const val Y0 = 2731 - 50
            private const val DX = 146
            private const val DY = 146

            
            fun isInBoard(x: Int, y: Int): Boolean = x in 1..19 && y in 1..19
        }
        
        data class EatCount(
            var black: Int = 0,
            var white: Int = 0
        ) {

            operator fun plusAssign(eatCount: EatCount) {
                black += eatCount.black
                white += eatCount.white
            }
            
            fun isEmpty(): Boolean = black == 0 && white == 0
            
        }
        
    }
    
    enum class ChessType(val id: Int) {
        EMPTY(0),
        BLACK(1),
        WHITE(2);
        
        
        fun getOpposite(): ChessType =
            when(this) {
                EMPTY -> EMPTY
                BLACK -> WHITE
                WHITE -> BLACK
            }
    }
    
}