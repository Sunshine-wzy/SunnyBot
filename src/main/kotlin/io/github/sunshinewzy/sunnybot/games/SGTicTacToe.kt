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
 * ������
 */
object SGTicTacToe : SGroupGame("������", RunningState.TICTACTOE, RunningState.TICTACTOE_WAITING) {
    private val wrongFormatMsg = """
        ���������Ϣ��ʽ����ȷ
        ���������� "#x,y" ������
        
        Tips:
        �� xΪ������, yΪ������
        �� x��y��Ϊ1-3֮�������
        �� ,ΪӢ�Ķ���
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
                    
                    ��û�вμӾ�������Ϸ��
                    ��ǰ��ң�
                    �� ${p1.nameCard} (${p1.id})
                    �� ${p2.nameCard} (${p2.id})
                """.trimIndent())
            )
            return
        }
        
        if(p != dataTicTacToe.round){
            group.sendMsg(name, At(member) +
                PlainText("\n��ǰ�� ") + At(player[dataTicTacToe.round]!!) +
                PlainText(" " + """
                    �Ļغ�
                    ���������ӣ������ĵȴ���
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
                    ���̸���ʧ�ܣ�
                    ��ȷ�����������ڲ�
                    (1 �� x, y �� 3)
                    �����ظ�����
                """.trimIndent())
                return
            }
        }
        else if(id == p2.id){
            if(!updateData(dataTicTacToe, 2, x, y, order)) {
                group.sendMsg(name, """
                    ���̸���ʧ�ܣ�
                    ��ȷ�����������ڲ�
                    (1 �� x, y �� 3)
                    �����ظ�����
                """.trimIndent())
                return
            }
        }
        group.sendMsg(name, group.laTeXImage(printBoard(dataTicTacToe)))
        
        if(judge(group, dataTicTacToe))
            return
        
        //�غϸ���
        if(dataTicTacToe.round == 1)
            dataTicTacToe.round = 2
        else if(dataTicTacToe.round == 2)
            dataTicTacToe.round = 1

        group.sendMsg(name, At(dataTicTacToe.player[dataTicTacToe.round]!!) +
            PlainText("\n�����Ļغ��ˣ������� #x,y (x��y��Ϊ1-3֮�������) ������")
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
                "������",
                At(member) + PlainText(
                    "���1�Ѿ�λ\n" +
                        "�ȴ���2λ������� #������ ..."
                )
            )
            sDataGroup.runningState = RunningState.TICTACTOE_WAITING
        } else if(sDataGroup.runningState == RunningState.TICTACTOE_WAITING) {
            val p1 = dataTicTacToe.player[1] ?: kotlin.run { 
                group.sendMsg(name, "���1���󲻴��ڣ���ҳ�ʼ��ʧ�ܣ�\n������ ��Ϸ����")
                group.setRunningState(RunningState.FREE)
                return
            }
            if(member.id == p1.id){
                group.sendMsg(name, At(member) +
                    PlainText(" �������������֣�")
                )
                return
            }
            
            dataTicTacToe.player[2] = member
            if(dataTicTacToe.player[1] == null || dataTicTacToe.player[2] == null){
                group.sendMsg(name, "��ҳ�ʼ��ʧ�ܣ���Ϸ������")
                sDataGroup.runningState = RunningState.FREE
                return
            }

            sDataGroup.runningState = RunningState.TICTACTOE
            group.sendMsg(
                name,
                At(dataTicTacToe.player[1]!!) + PlainText(" ") +
                    At(dataTicTacToe.player[2]!!) + PlainText(
                    """
                        
                        ���1��2���Ѿ�λ
                        ��Ϸ��ʼ��
                        ������ "#x,y" ������
                        
                        Tips:
                        �� xΪ������, yΪ������
                        �� x��y��Ϊ1-3֮�������
                        �� ,ΪӢ�Ķ���
                    """.trimIndent()
                )
            )
            
            val round = init(dataTicTacToe)
            group.sendMsg(name, group.laTeXImage(printBoard(dataTicTacToe)))
            group.sendMsg(name, At(dataTicTacToe.player[round]!!) +
                PlainText("\n�������֣������� #x,y (x��y��Ϊ1-3֮�������) ������")
            )
        }
    }

    /**
     * ʤ���ж�
     */
    private suspend fun judge(group: Group, dataTicTacToe: DataTicTacToe): Boolean {
        val slot = dataTicTacToe.slot
        //��
        for(i in 1..3){
            if(slot[i] != 0 && slot[i] == slot[i+3] && slot[i] == slot[i+6]){
                win(group, slot[i], dataTicTacToe)
                return true
            }
        }
        //��
        for(i in 1..7 step 3){
            if(slot[i] != 0 && slot[i] == slot[i+1] && slot[i] == slot[i+2]){
                win(group, slot[i], dataTicTacToe)
                return true
            }
        }
        //б
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
        
        //ƽ���ж�
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
            
            group.sendMsg(name, At(p1) + " �� " + At(p2) +
                """

                    ���ƽ�֣�
                    ��Ϸ����: $reward STD
                """.trimIndent()
            )
            return true
        }
        
        return false
    }
    
    private suspend fun win(group: Group, p: Int, dataTicTacToe: DataTicTacToe) {
        val winner = dataTicTacToe.player[p] ?: kotlin.run {
            sDataGroupMap[group.id]?.runningState = RunningState.FREE
            group.sendMsg(name, "��Ҷ��󲻴��ڣ�ʤ���ж�ʧ�ܣ�\n������ ��Ϸ����")
            return
        }

        group.setRunningState(RunningState.FREE)
        val reward = Random.nextInt(5) + 6
        winner.addSTD(reward)
        
        group.sendMsg(name, PlainText("��ϲ��� ") + At(winner) +
            PlainText("""
                
                ���ʤ����
                ��Ϸ����: $reward STD
            """.trimIndent())
        )
    }
    
    /**
     * <NPC����>
     * 
     * �Կ����� ����С�����㷨 alpha-beta��֦
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
                args[i] = "��"
            if(slot[i] == 1)
                args[i] = "��"
            else if(slot[i] == 2)
                args[i] = "��"
        }

        return """
            \begin{array}{1}
            y�vx & 1 & 2 & 3\\
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
        //��������
        if(dataTicTacToe.slot[order] != 0)
            return false
        
        dataTicTacToe.slot[order] = p
        return true
    }
}