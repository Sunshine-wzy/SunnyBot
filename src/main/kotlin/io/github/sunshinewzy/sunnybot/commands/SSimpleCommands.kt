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
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t�� SunnyBot ��\n" +
            "1. 24��" +
            "\n\n===============\n" +
            "������  #��������  �Կ�ʼ\n" +
            "[��] #24��")
    }
}

object SCInfo: SimpleCommand(
    PluginMain,
    "info", "��Ϣ",
    description = "��ѯ������Ϣ"
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
        sendMessage(At(member.group[id]).plus(PlainText("����STDΪ: ${sPlayer.std}")))
    }
}