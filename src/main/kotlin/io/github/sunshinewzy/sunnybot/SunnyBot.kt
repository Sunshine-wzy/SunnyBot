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
        (contains("���Ӳ���")) end@{
            val id = getGroupID(sender)
            if (id == 0L)
                return@end

            reply("��Ϸ����")
            groups[id]?.runningState = ""
        }

        (contains("�����ڰ�")) startAgain@{
            val group = getGroup(sender) ?: return@startAgain

            when (groups[getGroupID(sender)]?.runningState) {
                "24��" -> startHour24(group)

                else -> reply("��ǰû����Ϸ���ڽ��С�")
            }
        }


//        (contains("sunshine") or contains("����") or startsWith("#")) game@{
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