package io.github.sunshinewzy.sunnybot.functions

import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SPlayerData
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText
import java.util.*
import java.util.stream.Collectors

// region Hour24 - 24��

private val hour24characters = listOf(
    '+', '-', '*', '/', '(', ')'
)

fun hour24() {

    miraiBot?.subscribeMessages {

        (contains("sunny") or contains("����") or startsWith("#")) hour@{
            if (sender !is Member)
                return@hour
            val member = sender as Member
            val group = member.group
            val id = group.id
            if(!sGroupMap.containsKey(id))
                sGroupMap[id] = SGroup(id)
            val sGroup = sGroupMap[id]!!
            var msg = this.message[PlainText.Key]?.contentToString()
            
            regPlayer(member)

            if (msg != null) {
                if (msg.startsWith("#")) msg = msg.substring(1)
                msg = msg.replace(" ", "")

                when (sGroup.runningState) {
                    "24��" -> {
                        //���ʽ�Ϸ��Գ������
                        var tmp = 0
                        while (tmp < msg.length) {
                            val ch = msg[tmp]
                            if (!(hour24characters.contains(
                                    ch
                                ) || ch in '0'..'9')
                            ) {
                                reply(
                                    "������ı����� ֻ�ܺ��з���+ - * / ( )�Լ�����" +
                                            sGroup.hour24[1] + " " + sGroup.hour24[2] + " " + sGroup.hour24[3] + " " + sGroup.hour24[4]
                                )
                                return@hour
                            }
                            tmp++
                        }
                        msg = "($msg)"

                        //ջ�㷨
                        val number = Stack<Int>()
                        val operator = Stack<Char>()
                        var i = 0
                        var cnt = 0
                        while (i < msg.length) {
                            //�����Ŵ���
                            while (msg[i] == '(') {
                                operator.push(msg[i])
                                i++
                            }

                            //��������ջ
                            var x = 0
                            while (msg[i] in '0'..'9')
                                x = x * 10 + (msg[i++].toInt() - '0'.toInt())
                            if (!isHour24(x, sGroup)) {
                                //���ʽ�Ϸ��Զ��μ��
                                reply(
                                    "������ı����� ֻ�ܺ��з���+ - * / ( )�Լ�����" +
                                            sGroup.hour24[1] + "  " + sGroup.hour24[2] + "  " + sGroup.hour24[3] + "  " + sGroup.hour24[4]
                                )
                                return@hour
                            }
                            if (number.contains(x)) {
                                reply("�����ظ�ʹ������")
                                return@hour
                            }
                            cnt++
                            number.push(x)
                            do {
                                //�����Ŵ���
                                if (msg[i] == ')') {
                                    while (operator.peek() != '(') popHour24(
                                        number,
                                        operator
                                    )
                                    operator.pop()
                                } else {
                                    //���ݱ�־����ֵ���������ջ���ջ���㴦��
                                    while (canHour24(
                                            msg,
                                            i,
                                            number,
                                            operator
                                        )
                                    ) if (!popHour24(
                                            number,
                                            operator
                                        )
                                    ) {
                                        reply("0�������������������ı��ʽ�Ƿ���ȷ")
                                        return@hour
                                    }
                                    operator.push(msg[i])
                                }
                                i++
                            } while (i < msg.length && msg[i - 1] == ')')
                        }
                        if (cnt != 4) {
                            reply("�뽫4����ȫ������ ��ÿ����ֻ����һ��")
                            return@hour
                        }
                        reply(senderName + "�ı��ʽ������Ϊ: " + number[0])
                        if (number[0] == 24) {
                            val rewardSTD = Random().nextInt(5) + 6
                            SPlayerData.sPlayerMap[member.id]!!.std += rewardSTD
                            reply("��ϲ��� $senderName ���ʤ����\n"
                                + "��ý���: $rewardSTD STD")
                            sGroup.runningState = ""
                        } else {
                            reply("$senderName �𰸴���")
                        }
                    }
                    
                    
                    else -> {
                        if (msg.contains("24��"))
                            startHour24(group)
                    }
                }

                return@hour
            }

        }
    }

}

suspend fun startHour24(group: Group) {
    val id = group.id

    if (!sGroupMap.containsKey(id))
        sGroupMap[id] = SGroup(id)
    val sGroup = sGroupMap[id] ?: return

    sGroup.runningState = "24��"
    for (i in 0..4) {
        sGroup.hour24[i] = -1
    }

    val rand = Random()
    for (i in 1..4) {
        var temp: Int
        do {
            temp = rand.nextInt(13) + 1
        } while (isHour24(temp, sGroup))
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
    number: Stack<Int>,
    operator: Stack<Char>
): Boolean {
    val ch = s[i]
    if ((ch == '+' || ch == '-') && operator.peek() != '(') return true
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
            if (num == 0) return false
            number[number.size - 1] = number.peek() / num
        }
    }
    return true
}

// endregion