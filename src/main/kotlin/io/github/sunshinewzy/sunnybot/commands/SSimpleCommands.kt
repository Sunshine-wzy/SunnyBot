package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

/**
 * Sunny Commands
 */

suspend fun regSSimpleCommands() {
    //ָ��ע��
    //Ĭ��m*Ϊ����ȺԱ u*Ϊ�����û�
    SCMenu.reg()
    SCInfo.reg("u*")
    SCAntiRecall.reg()
    SCServerInfo.reg("u*")
}


object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd", "�˵�", "����",
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t�� SunnyBot ��\n" +
            "�� 24��" +
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
        if(user == null)
            return
        val player = user!!
        val id = player.id
        
        if(!sPlayerMap.containsKey(id)){
            regPlayer(player)
        }
        val sPlayer = sPlayerMap[id]!!
        
        if(user is Member){
            val member = user as Member
            sendMessage(At(member.group[id]).plus(PlainText("����STDΪ: ${sPlayer.std}")))
            return
        }
        sendMessage("����STDΪ: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "antirecall", "atrc", "������",
    description = "����/�رշ�����"
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator()){
            val msg = str.toLowerCase()
            if(msg.contains("��") || msg.contains("t"))
                antiRecall?.setAntiRecallStatus(group.id, true)
            else if(msg.contains("��") || msg.contains("f"))
                antiRecall?.setAntiRecallStatus(group.id, false)
            sendMessage("������״̬Ϊ: ${antiRecall?.checkAntiRecallStatus(group.id)}")
        }
        else{
            sendMessage(At(member).plus(PlainText("������Ⱥ�������Ա��û������/�رշ����ع��ܵ�Ȩ�ޣ�")))
        }
    }
}

object SCServerInfo: SimpleCommand(
    PluginMain,
    "serverinfo", "server", "������", "������״̬", "״̬",
    description = "������״̬��ѯ"
) {
    private const val url = "https://mc.iroselle.com/api/data/getServerInfo"
    
    @Handler
    suspend fun CommandSender.handle() {
        val result = SRequest(url).result("happylandmc.cc", 0)
        sendMessage(result)
    }
}