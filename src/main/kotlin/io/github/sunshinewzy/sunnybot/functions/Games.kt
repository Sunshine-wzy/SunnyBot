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

// region Hour24 - 24点

private val hour24characters = listOf(
    '+', '-', '*', '/', '(', ')'
)

fun hour24() {

    miraiBot?.subscribeMessages {

        (contains("sunny") or contains("阳光") or startsWith("#")) hour@{
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
                    "24点" -> {
                        //表达式合法性初步检查
                        var tmp = 0
                        while (tmp < msg.length) {
                            val ch = msg[tmp]
                            if (!(hour24characters.contains(
                                    ch
                                ) || ch in '0'..'9')
                            ) {
                                reply(
                                    "输入的文本错误 只能含有符号+ - * / ( )以及数字" +
                                            sGroup.hour24[1] + " " + sGroup.hour24[2] + " " + sGroup.hour24[3] + " " + sGroup.hour24[4]
                                )
                                return@hour
                            }
                            tmp++
                        }
                        msg = "($msg)"

                        //栈算法
                        val number = Stack<Int>()
                        val operator = Stack<Char>()
                        var i = 0
                        var cnt = 0
                        while (i < msg.length) {
                            //左括号处理
                            while (msg[i] == '(') {
                                operator.push(msg[i])
                                i++
                            }

                            //操作数入栈
                            var x = 0
                            while (msg[i] in '0'..'9')
                                x = x * 10 + (msg[i++].toInt() - '0'.toInt())
                            if (!isHour24(x, sGroup)) {
                                //表达式合法性二次检查
                                reply(
                                    "输入的文本错误 只能含有符号+ - * / ( )以及数字" +
                                            sGroup.hour24[1] + "  " + sGroup.hour24[2] + "  " + sGroup.hour24[3] + "  " + sGroup.hour24[4]
                                )
                                return@hour
                            }
                            if (number.contains(x)) {
                                reply("请勿重复使用数字")
                                return@hour
                            }
                            cnt++
                            number.push(x)
                            do {
                                //右括号处理
                                if (msg[i] == ')') {
                                    while (operator.peek() != '(') popHour24(
                                        number,
                                        operator
                                    )
                                    operator.pop()
                                } else {
                                    //根据标志函数值作运算符入栈或出栈运算处理
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
                                        reply("0不能作除数！请检查您的表达式是否正确")
                                        return@hour
                                    }
                                    operator.push(msg[i])
                                }
                                i++
                            } while (i < msg.length && msg[i - 1] == ')')
                        }
                        if (cnt != 4) {
                            reply("请将4个数全部用上 且每个数只能用一次")
                            return@hour
                        }
                        reply(senderName + "的表达式计算结果为: " + number[0])
                        if (number[0] == 24) {
                            val rewardSTD = Random().nextInt(5) + 6
                            SPlayerData.sPlayerMap[member.id]!!.std += rewardSTD
                            reply("恭喜玩家 $senderName 获得胜利！\n"
                                + "获得奖励: $rewardSTD STD")
                            sGroup.runningState = ""
                        } else {
                            reply("$senderName 答案错误")
                        }
                    }
                    
                    
                    else -> {
                        if (msg.contains("24点"))
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

    sGroup.runningState = "24点"
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
        "=====24点-游戏开始=====\n"
                + "输入\"再来亿把\"以重新开始\n"
                + "输入\"老子不会\"以结束游戏\n"
                + "请用下面给出的4个数通过+ - * /四种运算\n"
                + "以及()求出24即为胜利\n"
                + "输入#后接你的答案\n\n"
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

//判断运算符的优先级别，建立标志函数
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

//运算符栈顶元素出栈，并取出操作数栈元素完成相应的运算
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