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
 * 24点
 */
object SGHour24 : SGroupGame("24点", RunningState.HOUR24) {
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
                    "输入的文本错误 只能含有符号+ - * / ( )以及数字" +
                        event.sDataGroup.hour24[1] + " " + event.sDataGroup.hour24[2] + " " + event.sDataGroup.hour24[3] + " " + event.sDataGroup.hour24[4]
                )
                return
            }
            tmp++
        }
        str = "($str)"

        //栈算法
        val number = Stack<Int>()
        val operator = Stack<Char>()
        var i = 0
        var cnt = 0
        val existNumber = HashSet<Int>()
        
        while(i < str.length) {
            //左括号处理
            while(str[i] == '(') {
                operator.push(str[i])
                i++
            }

            //操作数入栈
            var x = 0
            while(str[i] in '0'..'9')
                x = x * 10 + (str[i++].code - '0'.code)
            if(!isHour24(x, event.sDataGroup)) {
                //表达式合法性二次检查
                event.group.sendMessage(
                    "输入的文本错误 只能含有符号+ - * / ( )以及数字" +
                        event.sDataGroup.hour24[1] + "  " + event.sDataGroup.hour24[2] + "  " + event.sDataGroup.hour24[3] + "  " + event.sDataGroup.hour24[4]
                )
                return
            }
            if(existNumber.contains(x)) {
                event.group.sendMessage("请勿重复使用数字")
                return
            }
            cnt++
            existNumber += x
            number.push(x)
            
            do {
                //右括号处理
                if(str[i] == ')') {
                    while(operator.peek() != '(') {
                        popHour24(number, operator)
                    }
                    operator.pop()
                } else {
                    //根据标志函数值作运算符入栈或出栈运算处理
                    while(canHour24(str, i, operator)) {
                        if(!popHour24(number, operator)) {
                            event.group.sendMessage("0不能作除数！请检查您的表达式是否正确")
                            return
                        }
                    }
                    operator.push(str[i])
                }
                i++
            } while(i < str.length && str[i - 1] == ')')
        }

        if(cnt != 4) {
            event.group.sendMessage("请将4个数全部用上 且每个数只能用一次")
            return
        }

        event.group.sendMsg(name, At(event.member) + " 的表达式计算结果为: ${number[0]}")
        if(number[0] == 24) {
            val rewardSTD = kotlin.random.Random.nextInt(5) + 6
            event.member.addSTD(rewardSTD)
            event.group.sendMsg(name, buildMessageChain {
                +PlainText("恭喜玩家 ")
                +At(event.member)
                +" 获得胜利！\n获得奖励: $rewardSTD STD"
            }
            )
            event.group.setRunningState(RunningState.FREE)
        } else {
            event.group.sendMessage("${event.member.nameCardOrNick} 答案错误")
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
               输入"再来亿把"以重新开始
               输入"老子不会"以结束游戏
               请用下面给出的4个数通过+ - * /四种运算
               以及()求出24即为胜利
               输入#后接你的答案
               
               ${sDataGroup.hour24[1]}  ${sDataGroup.hour24[2]}  ${sDataGroup.hour24[3]}  ${sDataGroup.hour24[4]}
            """.trimIndent()
        )
    }

    private fun isHour24(num: Int, sDataGroup: SDataGroup): Boolean {
        return sDataGroup.hour24.contains(num)
    }

    //判断运算符的优先级别，建立标志函数
    private fun canHour24(
        s: String,
        i: Int,
        operator: Stack<Char>
    ): Boolean {
        val ch = s[i]
        if((ch == '+' || ch == '-') && operator.peek() != '(') return true
        return (ch == '*' || ch == '/') && (operator.peek() == '*' || operator.peek() == '/')
    }

    //运算符栈顶元素出栈，并取出操作数栈元素完成相应的运算
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