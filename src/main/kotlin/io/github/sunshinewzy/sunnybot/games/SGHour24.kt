package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SGroupData
import io.github.sunshinewzy.sunnybot.objects.SPlayerData
import java.util.*
import java.util.stream.Collectors

/**
 * 24��
 */
object SGHour24 : SGroupGame("24��") {
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
                        event.sGroup.hour24[1] + " " + event.sGroup.hour24[2] + " " + event.sGroup.hour24[3] + " " + event.sGroup.hour24[4]
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
        while(i < str.length) {
            //�����Ŵ���
            while(str[i] == '(') {
                operator.push(str[i])
                i++
            }

            //��������ջ
            var x = 0
            while(str[i] in '0'..'9')
                x = x * 10 + (str[i++].toInt() - '0'.toInt())
            if(!isHour24(x, event.sGroup)) {
                //���ʽ�Ϸ��Զ��μ��
                event.group.sendMessage(
                    "������ı����� ֻ�ܺ��з���+ - * / ( )�Լ�����" +
                        event.sGroup.hour24[1] + "  " + event.sGroup.hour24[2] + "  " + event.sGroup.hour24[3] + "  " + event.sGroup.hour24[4]
                )
                return
            }
            if(number.contains(x)) {
                event.group.sendMessage("�����ظ�ʹ������")
                return
            }
            cnt++
            number.push(x)
            do {
                //�����Ŵ���
                if(str[i] == ')') {
                    while(operator.peek() != '(') popHour24(
                        number,
                        operator
                    )
                    operator.pop()
                } else {
                    //���ݱ�־����ֵ���������ջ���ջ���㴦��
                    while(canHour24(
                            str,
                            i,
                            operator
                        )
                    ) if(!popHour24(
                            number,
                            operator
                        )
                    ) {
                        event.group.sendMessage("0�������������������ı��ʽ�Ƿ���ȷ")
                        return
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

        event.group.sendMessage(event.member.nameCard + "�ı��ʽ������Ϊ: " + number[0])
        if(number[0] == 24) {
            val rewardSTD = Random().nextInt(5) + 6
            SPlayerData.sPlayerMap[event.member.id]!!.std += rewardSTD
            event.group.sendMessage(
                "��ϲ��� ${event.member.nameCard} ���ʤ����\n"
                    + "��ý���: $rewardSTD STD"
            )
            event.sGroup.runningState = ""
        } else {
            event.group.sendMessage("${event.member.nameCard} �𰸴���")
        }
    }

    override suspend fun startGame(event: SGroupGameEvent) {
        val group = event.group
        val id = group.id

        if(!SGroupData.sGroupMap.containsKey(id))
            SGroupData.sGroupMap[id] = SGroup(id)
        val sGroup = SGroupData.sGroupMap[id] ?: return

        sGroup.runningState = name
        for(i in 0..4) {
            sGroup.hour24[i] = -1
        }

        val rand = Random()
        for(i in 1..4) {
            var temp: Int
            do {
                temp = rand.nextInt(13) + 1
            } while(isHour24(temp, sGroup))
            sGroup.hour24[i] = temp
        }

        group.sendMessage(
            "=====24��-��Ϸ��ʼ=====\n"
                + "����\"�����ڰ�\"�����¿�ʼ\n"
                + "����\"���Ӳ���\"�Խ�����Ϸ\n"
                + "�������������4����ͨ��+ - * /��������\n"
                + "�Լ�()���24��Ϊʤ��\n"
                + "����#�����Ĵ�\n\n"
                + sGroup.hour24[1] + "  " + sGroup.hour24[2] + "  " + sGroup.hour24[3] + "  " + sGroup.hour24[4]
                + "\n==============="
        )
    }

    private fun isHour24(num: Int, sGroup: SGroup): Boolean {
        val listHour24: List<Int> =
            Arrays.stream(sGroup.hour24).boxed()
                .collect(
                    Collectors.toList()
                )
        return listHour24.contains(num)
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
            '+' -> number[number.size - 1] = number.peek() + num
            '-' -> number[number.size - 1] = number.peek() - num
            '*' -> number[number.size - 1] = number.peek() * num
            '/' -> {
                if(num == 0) return false
                number[number.size - 1] = number.peek() / num
            }
        }
        return true
    }
}