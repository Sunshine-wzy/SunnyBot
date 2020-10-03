package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.regSSimpleCommands
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.functions.hour24
import io.github.sunshinewzy.sunnybot.functions.startHour24
import io.github.sunshinewzy.sunnybot.objects.groups
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.subscribeMessages

val miraiScope = CoroutineScope(SupervisorJob())
var antiRecall: AntiRecall? = null

suspend fun sunnyInit() {
    regCmd()
    regSSimpleCommands()
}

private fun regCmd() {
    miraiBot?.subscribeMessages {
        (contains("老子不会")) end@{
            val id = getGroupID(sender)
            if (id == 0L)
                return@end

            reply("游戏结束")
            groups[id]?.runningState = ""
        }

        (contains("再来亿把")) startAgain@{
            val group = getGroup(sender) ?: return@startAgain

            when (groups[getGroupID(sender)]?.runningState) {
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