package io.github.sunshinewzy.sunnybot.games.game

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.games.SGroupGame
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.sendMsg
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import java.util.*

/**
 * 24��
 */
object SGHour24 : SGroupGame("24��", RunningState.HOUR24) {
    private val hour24characters = listOf(
        '+', '-', '*', '/', '(', ')'
    )

    override suspend fun runGame(event: SGroupGameEvent) {
        var str = event.msg
        if(str.startsWith("#")) str = str.substring(1)
        str = str.replace(" ", "")

        var tmp = 0
        while(tmp < str.length) {
            val ch = str[tmp]
            if(!hour24characters.contains(ch) && ch !in '0'..'9') {
                event.group.sendMessage(
                    "������ı����� ֻ�ܺ��з���+ - * / ( )�Լ�����" +
                        event.sDataGroup.hour24[1] + " " + event.sDataGroup.hour24[2] + " " + event.sDataGroup.hour24[3] + " " + event.sDataGroup.hour24[4]
                )
                return
            }
            tmp++
        }
        str = "($str)"

        //ջ�㷨
        val number = Stack<Int>()
        val operator = Stack<Char>()
        var i = 0
        var cnt = 0
        val existNumber = HashSet<Int>()
        
        while(i < str.length) {
            //�����Ŵ���
            while(str[i] == '(') {
                operator.push(str[i])
                i++
            }

            //��������ջ
            var x = 0
            while(str[i] in '0'..'9')
                x = x * 10 + (str[i++].code - '0'.code)
            if(!isHour24(x, event.sDataGroup)) {
                //���ʽ�Ϸ��Զ��μ��
                event.group.sendMessage(
                    "������ı����� ֻ�ܺ��з���+ - * / ( )�Լ�����" +
                        event.sDataGroup.hour24[1] + "  " + event.sDataGroup.hour24[2] + "  " + event.sDataGroup.hour24[3] + "  " + event.sDataGroup.hour24[4]
                )
                return
            }
            if(existNumber.contains(x)) {
                event.group.sendMessage("�����ظ�ʹ������")
                return
            }
            cnt++
            existNumber += x
            number.push(x)
            
            do {
                //�����Ŵ���
                if(str[i] == ')') {
                    while(operator.peek() != '(') {
                        popHour24(number, operator)
                    }
                    operator.pop()
                } else {
                    //���ݱ�־����ֵ���������ջ���ջ���㴦��
                    while(canHour24(str, i, operator)) {
                        if(!popHour24(number, operator)) {
                            event.group.sendMessage("0�������������������ı��ʽ�Ƿ���ȷ")
                            return
                        }
                    }
                    operator.push(str[i])
                }
                i++
            } while(i < str.length && str[i - 1] == ')')
        }

        if(cnt != 4) {
            event.group.sendMessage("�뽫4����ȫ������ ��ÿ����ֻ����һ��")
            return
        }

        event.group.sendMsg(name, At(event.member) + " �ı��ʽ������Ϊ: ${number[0]}")
        if(number[0] == 24) {
            val rewardSTD = kotlin.random.Random.nextInt(5) + 6
            event.member.addSTD(rewardSTD)
            event.group.sendMsg(name, buildMessageChain {
                +PlainText("��ϲ��� ")
                +At(event.member)
                +" ���ʤ����\n��ý���: $rewardSTD STD"
            }
            )
            event.group.setRunningState(RunningState.FREE)
        } else {
            event.group.sendMessage("${event.member.nameCardOrNick} �𰸴���")
        }
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        val group = event.group
        val id = group.id

        if(!SSaveGroup.sGroupMap.containsKey(id))
            SSaveGroup.sGroupMap[id] = SGroup(id)
        val sGroup = SSaveGroup.sGroupMap[id] ?: return
        val sDataGroup = event.sDataGroup

        sDataGroup.runningState = RunningState.HOUR24
        sDataGroup.lastRunningState = RunningState.HOUR24
        with(sDataGroup.players) {
            clear()
            add(event.member.id)
        }
        
        for(i in 0..4) {
            sDataGroup.hour24[i] = -1
        }

        val rand = Random()
        for(i in 1..4) {
            var temp: Int
            do {
                temp = rand.nextInt(13) + 1
            } while(isHour24(temp, sDataGroup))
            sDataGroup.hour24[i] = temp
        }

        group.sendMsg(name, """
               ����"�����ڰ�"�����¿�ʼ
               ����"���Ӳ���"�Խ�����Ϸ
               �������������4����ͨ��+ - * /��������
               �Լ�()���24��Ϊʤ��
               ����#�����Ĵ�
               
               ${sDataGroup.hour24[1]}  ${sDataGroup.hour24[2]}  ${sDataGroup.hour24[3]}  ${sDataGroup.hour24[4]}
            """.trimIndent()
        )
    }

    private fun isHour24(num: Int, sDataGroup: SDataGroup): Boolean {
        return sDataGroup.hour24.contains(num)
    }

    //�ж�����������ȼ��𣬽�����־����
    private fun canHour24(
        s: String,
        i: Int,
        operator: Stack<Char>
    ): Boolean {
        val ch = s[i]
        if((ch == '+' || ch == '-') && operator.peek() != '(') return true
        return (ch == '*' || ch == '/') && (operator.peek() == '*' || operator.peek() == '/')
    }

    //�����ջ��Ԫ�س�ջ����ȡ��������ջԪ�������Ӧ������
    private fun popHour24(number: Stack<Int>, operator: Stack<Char>): Boolean {
        val num = number.pop()
        when (operator.pop()) {
            '+' -> number[number.lastIndex] = number.peek() + num
            '-' -> number[number.lastIndex] = number.peek() - num
            '*' -> number[number.lastIndex] = number.peek() * num
            '/' -> {
                if(num == 0) return false
                number[number.lastIndex] = number.peek() / num
            }
        }
        return true
    }
}