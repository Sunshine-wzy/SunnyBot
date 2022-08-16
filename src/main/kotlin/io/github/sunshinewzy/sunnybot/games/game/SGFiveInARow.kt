package io.github.sunshinewzy.sunnybot.games.game

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.games.SGroupGame
import io.github.sunshinewzy.sunnybot.games.game.SGFiveInARow.ChessBoard.Direction.*
import io.github.sunshinewzy.sunnybot.games.game.SGFiveInARow.ChessType.*
import io.github.sunshinewzy.sunnybot.objects.SCoordinate
import io.github.sunshinewzy.sunnybot.objects.addSTD
import io.github.sunshinewzy.sunnybot.objects.setRunningState
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.toInputStream
import io.github.sunshinewzy.sunnybot.utils.SImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.image.BufferedImage
import kotlin.random.Random

object SGFiveInARow : SGroupGame("������", RunningState.FIVE_IN_A_ROW) {
    private val imgChessBoard = SImage.loadImage("Chess/ChessBoard.png")
    private val imgBlackPieces = SImage.loadImage("Chess/BlackPieces.png")
    private val imgWhitePieces = SImage.loadImage("Chess/WhitePieces.png")
    private val wrongFormatMsg = """
        �����͵�ָ���ʽ����ȷ
        �����·������� "#A1" ��ָ��������
        
        Tips:
        �� A ������Ϊ A-S ֮�������һ����ĸ
        �� 1 ������Ϊ 1-19 ֮�������һ������
        �� �����пո񣬲����ִ�Сд
    """.trimIndent()
    
    
    override suspend fun runGame(event: SGroupGameEvent) {
        val dataChess = event.sDataGroup.fiveInARow
        var str = event.msg
        if(str.startsWith("#")) str = str.substring(1)
        else return
        str = str.replace(" ", "").uppercase()
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
                    
                    ��û�вμ���������ġ�
                    ��ǰ��ң�
                    �� ${p1.nameCardOrNick} (${p1.id})
                    �� ${p2.nameCardOrNick} (${p2.id})
                """.trimIndent())
            )
            return
        }

        if(p != board.round.id){
            group.sendMsg(
                name, At(member) +
                PlainText("\n��ǰ��\n") + At(player[board.round.id]!!) +
                PlainText("""
                    
                    �Ļغ�
                    ���������ӣ������ĵȴ���
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

        //���ӺϷ��Լ���
        if(!board.placePieces(type, x, y)) {
            group.sendMsg(
                name, """
                    ���̸���ʧ�ܣ�
                    ��ȷ�����������λ��������
                """.trimIndent())
            return
        }

        val image = board.printBoard().toInputStream()?.uploadAsImage(group) ?: return
        group.sendMsg(name, image)

        if(board.judge(x, y)) {
            val winner = dataChess.players[board.round.id]!!
            group.setRunningState(RunningState.FREE)

            val reward = Random.nextInt(5) + 8
            winner.addSTD(reward)

            group.sendMsg(
                name, PlainText("��ϲ��� ") + At(winner) +
                PlainText("""
                
                ���ʤ����
                ��Ϸ����: $reward STD
            """.trimIndent())
            )
            return
        }
        
        //�غϸ���
        if(board.round == BLACK)
            board.round = WHITE
        else if(board.round == WHITE)
            board.round = BLACK

        group.sendMsg(
            name, At(dataChess.players[board.round.id]!!) +
            PlainText("\n��ִ${if(board.round.id == 1) "����" else "����"}�������Ļغ��ˣ��뷢������ \"#A1\" ��ָ��������")
        )
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        event.apply {
            val dataChess = sDataGroup.fiveInARow
            
            if(sDataGroup.runningState == RunningState.FREE) {
                dataChess.players[1] = member
                group.sendMsg(name,
                    At(member) + PlainText(
                        "���1�Ѿ�λ\n" +
                            "�ȴ���2λ������� #������"
                    )
                )
                sDataGroup.runningState = RunningState.FIVE_IN_A_ROW_WAITING
            } else if(sDataGroup.runningState == RunningState.FIVE_IN_A_ROW_WAITING) {
                val p1 = dataChess.players[1] ?: kotlin.run {
                    group.sendMsg(name, "���1���󲻴��ڣ���ҳ�ʼ��ʧ�ܣ�\n������ ��Ϸ����")
                    group.setRunningState(RunningState.FREE)
                    return
                }
                if(member.id == p1.id){
                    group.sendMsg(
                        name, At(member) +
                        PlainText(" �������������֣�")
                    )
                    return
                }

                dataChess.players[2] = member
                if(dataChess.players[1] == null || dataChess.players[2] == null){
                    group.sendMsg(name, "��ҳ�ʼ��ʧ�ܣ���Ϸ������")
                    group.setRunningState(RunningState.FREE)
                    return
                }

                sDataGroup.runningState = RunningState.FIVE_IN_A_ROW
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
                        
                        ���1��2���Ѿ�λ
                        ��Ϸ��ʼ��
                        �뷢������ "#A1" ��ָ��������
                        
                        Tips:
                        �� A ������Ϊ A-S ֮�������һ����ĸ
                        �� 1 ������Ϊ 1-19 ֮�������һ������
                        �� �����пո񣬲����ִ�Сд
                    """.trimIndent()
                    )
                )
                
                dataChess.init()
                val image = dataChess.board?.printBoard()?.toInputStream()?.uploadAsImage(group) ?: return
                group.sendMsg(name, image)
                group.sendMsg(
                    name, At(dataChess.players[1]!!) +
                    PlainText("\n��ִ���ӣ��뷢������ \"#A1\" ��ָ��������")
                )
            }
        }
    }
    
    
    class ChessBoard {
        private val slots = Array(20) {
            Array(20) { EMPTY }
        }
        
        var round = BLACK
        val manual = ArrayList<String>()        //���׼�¼
        
        
        fun update() {
            
        }

        /**
         * ����ʱ�����ж�
         * ʤ���ж�
         */
        fun judge(x: Int, y: Int): Boolean {
            if(judgeByDirection(SCoordinate(x, y), RIGHT) + judgeByDirection(SCoordinate(x, y), LEFT) >= JUDGE_COUNT)
                return true

            if(judgeByDirection(SCoordinate(x, y), UP) + judgeByDirection(SCoordinate(x, y), DOWN) >= JUDGE_COUNT)
                return true

            if(judgeByDirection(SCoordinate(x, y), UP_RIGHT) + judgeByDirection(SCoordinate(x, y), DOWN_LEFT) >= JUDGE_COUNT)
                return true

            if(judgeByDirection(SCoordinate(x, y), UP_LEFT) + judgeByDirection(SCoordinate(x, y), DOWN_RIGHT) >= JUDGE_COUNT)
                return true

            return false
        }
        
        fun judgeByDirection(coordinate: SCoordinate, direction: Direction): Int {
            coordinate.x += direction.offsetX
            coordinate.y += direction.offsetY
            
            if(isInBoard(coordinate.x, coordinate.y) && slots[coordinate.x][coordinate.y] == round) {
                return judgeByDirection(coordinate, direction) + 1
            }
            
            return 0
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
            
            //���׼�¼
//            manual += "${if(type == BLACK) "��" else "��"},$x,$y"

            slots[x][y] = type
            return true
        }
        
        
        companion object {
            private const val X0 = 103 - 50
            private const val Y0 = 2731 - 50
            private const val DX = 146
            private const val DY = 146
            
            const val JUDGE_COUNT = 5 - 1

            
            fun isInBoard(x: Int, y: Int): Boolean = x in 1..19 && y in 1..19
        }
        
        
        enum class Direction(val offsetX: Int, val offsetY: Int) {
            RIGHT(1, 0), LEFT(-1, 0),
            UP(0, 1), DOWN(0, -1),
            UP_RIGHT(1, 1), DOWN_LEFT(-1, -1),
            UP_LEFT(-1, 1), DOWN_RIGHT(1, -1)
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