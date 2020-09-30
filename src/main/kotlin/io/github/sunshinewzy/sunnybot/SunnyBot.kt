package org.example.mirai.plugin.io.github.sunshinewzy.sunnybot

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import org.example.mirai.plugin.PluginMain

fun sunnyInit() {
    scMenu.register()
}


object scMenu : SimpleCommand(
    PluginMain,
    "menu", "cd",
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("-----=====SunshineBot=====-----\n"
            + "1. 24��")
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