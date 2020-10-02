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


        (contains("sunshine") or contains("阳光") or startsWith("#")) game@{
            if (sender !is Member)
                return@game
            val member = sender as Member
            val group = member.group
            val msg = this.message[PlainText.Key]?.contentToString()

            if (msg != null){
                if (msg.contains("24点"))
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
    description = "菜单"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("-----=====SunnyBot=====-----\n"
            + "1. 24点")

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