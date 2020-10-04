package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
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
    SCIpBind.reg()
}


object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd", "�˵�", "����",
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t�� SunnyBot ��\n" +
            "===============\n" +
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
    "serverinfo", "server", "zt", "������״̬", "״̬", "������",
    description = "������״̬��ѯ"
) {
    const val url = "https://mc.iroselle.com/api/data/getServerInfo"
    const val happylandIp = "happylandmc.cc"
    
    @Handler
    suspend fun CommandSender.handle() {
        var ip = happylandIp
        if(user == null)
            return
        if(user is Member){
            val member = user as Member
            val groupId = member.group.id
            if(sGroupMap.containsKey(groupId))
                ip = sGroupMap[groupId]!!.serverIp
        }
            
        val result = SRequest(url).result(ip, 0)
        val res = result.res

        var serverStatus = "����"
        if(res.server_status == 1)
            serverStatus = "����"

        sendMessage(
            "\t�� SunnyBot ��\n" +
            "������IP: $ip\n" +
            "������״̬: $serverStatus\n" +
            "��ǰ���������: ${res.server_player_online}\n" +
            "�����������: ${res.server_player_max}\n" +
            "�վ���������: ${res.server_player_average}\n" +
            "��ʷ���ͬʱ��������: ${res.server_player_history_max}\n" +
            "����ƽ����������: ${res.server_player_yesterday_average}\n" +
            "�������ͬʱ��������: ${res.server_player_yesterday_max}\n" +
            "����ʱ��: ${res.update_time}\n" +
            "��ѯ��ʱ: ${result.run_time}s")
    }
}

object SCIpBind: SimpleCommand(
    PluginMain,
    "ipbind", "ip", "��������", "��",
    description = "������״̬��ѯIP��"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        if(user !=null && user is Member){
            val member = user as Member
            val groupId = member.group.id
            if(sGroupMap.containsKey(groupId)) {
                val result = SRequest(SCServerInfo.url).result(serverIp, 0)
                if(result.code == 1){
                    sGroupMap[groupId]!!.serverIp = serverIp
                    sendMessage("$serverIp �󶨳ɹ���")
                    return
                }
            }
            else sGroupMap[groupId] = SGroup(groupId)
        }
        
        sendMessage("��ʧ��= =\n" +
            "��ȷ��������IP��ȷ��")
    }
}