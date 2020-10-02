package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.functions.hour24
import io.github.sunshinewzy.sunnybot.functions.startHour24
import io.github.sunshinewzy.sunnybot.groups.groups
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText

val miraiScope = CoroutineScope(SupervisorJob())

suspend fun sunnyInit() {
    SCMenu.register()

    ConsoleCommandSender.executeCommand("/permission permit m* io.github.sunshinewzy.sunnybot:command.menu")

    regCmd()
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


        (contains("sunshine") or contains("����") or startsWith("#")) game@{
            if (sender !is Member)
                return@game
            val member = sender as Member
            val group = member.group
            val msg = this.message[PlainText.Key]?.contentToString()

            if (msg != null){
                if (msg.contains("24��"))
                    startHour24(group)
            }
        }
    }

    hour24()
}

//[SunnyCommand]Menu
object SCMenu : SimpleCommand(
    PluginMain,
    "menu", "cd",
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("-----=====SunnyBot=====-----\n"
            + "1. 24��")

        miraiBot = Bot.botInstances[0]
    }
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