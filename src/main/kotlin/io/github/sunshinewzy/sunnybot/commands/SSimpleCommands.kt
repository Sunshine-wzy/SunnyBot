package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

/**
 * Sunny Commands
 */

suspend fun regSSimpleCommands() {
    SCMenu.reg()
    SCInfo.reg()
}

object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd",
    description = "菜单"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t『 SunnyBot 』\n" +
            "1. 24点" +
            "\n\n===============\n" +
            "请输入  #功能名称  以开始\n" +
            "[例] #24点")
    }
}

object SCInfo: SimpleCommand(
    PluginMain,
    "info", "信息",
    description = "查询个人信息"
) {
    @Handler
    suspend fun CommandSender.handle() {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val id = member.id
        
        if(!sPlayerMap.containsKey(id)){
            regPlayer(member)
        }

        val sPlayer = sPlayerMap[id]!!
        sendMessage(At(member.group[id]).plus(PlainText("您的STD为: ${sPlayer.std}")))
    }
}