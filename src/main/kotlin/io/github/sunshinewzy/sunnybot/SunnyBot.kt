package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.regSSimpleCommands
import io.github.sunshinewzy.sunnybot.commands.setPermit
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.functions.hour24
import io.github.sunshinewzy.sunnybot.functions.startHour24
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.subscribeMessages

val miraiScope = CoroutineScope(SupervisorJob())
var antiRecall: AntiRecall? = null

suspend fun sunnyInit() {
    //全局消息监听
    regMsg()
    //注册简单指令
    regSSimpleCommands()
    //设置权限
    setPermissions()
}

private fun regMsg() {
    miraiBot?.subscribeMessages {
        (contains("老子不会")) end@{
            val id = getGroupID(sender)
            if (id == 0L)
                return@end

            reply("游戏结束")
            sGroupMap[id]?.runningState = ""
        }

        (contains("再来亿把")) startAgain@{
            val group = getGroup(sender) ?: return@startAgain

            when (sGroupMap[getGroupID(sender)]?.runningState) {
                "24点" -> startHour24(group)

                else -> reply("当前没有游戏正在进行。")
            }
        }


//        (contains("sunshine") or contains("阳光") or startsWith("#")) game@{
//            if (sender !is Member)
//                return@game
//            val member = sender as Member
//            val group = member.group
//            val msg = this.message[PlainText.Key]?.contentToString()
//        }
    }

    hour24()
}

/**
 * |    被许可人类型    | 字符串表示示例 | 备注                                  |
 * |:----------------:|:-----------:|:-------------------------------------|
 * |      控制台       |   console   |                                      |
 * |      精确群       |   g123456   | 表示群, 而不表示群成员                   |
 * |      精确好友      |   f123456   | 必须通过好友消息                        |
 * |    精确临时会话    | t123456.789  | 群 123456 内的成员 789. 必须通过临时会话  |
 * |     精确群成员     | m123456.789 | 群 123456 内的成员 789. 同时包含临时会话. |
 * |      精确用户      |   u123456   | 同时包含群成员, 好友, 临时会话            |
 * |      任意群       |     g*      |                                      |
 * |  任意群的任意群员   |     m*      |                                      |
 * |  精确群的任意群员   |  m123456.*  | 群 123456 内的任意成员. 同时包含临时会话.  |
 * | 任意群的任意临时会话 |     t*      | 必须通过临时会话                        |
 * | 精确群的任意临时会话 |  t123456.*  | 群 123456 内的任意成员. 必须通过临时会话   |
 * |      任意好友      |     f*      |                                      |
 * |      任意用户      |     u*      | 任何人在任何环境                        |
 * |      任意对象      |      *      | 即任何人, 任何群, 控制台                 |
 */

suspend fun setPermissions() {
    setPermit("console:command.help", "u*")
    setPermit("*:*", "u1123574549")
}

fun getGroup(sender: User): Group? {
    if (sender is Member)
        return sender.group

    return null
}

fun getGroupID(sender: User): Long {
    if (sender is Member)
        return sender.group.id

    return 0
}