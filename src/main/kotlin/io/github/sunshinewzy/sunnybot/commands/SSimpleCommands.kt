package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyAdmins
import io.github.sunshinewzy.sunnybot.utils.SLaTeX
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.upload

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
    SCLaTeX.reg("u*")
}


object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd", "�˵�", "����",
    description = "�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("""
            �� SunnyBot ��
            ===============
            �� 24��
            �� ������

            ===============
            ������  #��������  �Կ�ʼ
            [��] #24��
        """.trimIndent()
        )
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
            sendMessage(PlainText("[Sunshine Technology Dollar]\n") + At(member.group[id]) + 
                PlainText("����STD���Ϊ: ${sPlayer.std}"))
            return
        }
        sendMessage("[Sunshine Technology Dollar]\n" +
            "����STD���Ϊ: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "antiRecall", "atrc", "������",
    description = "����/�رշ�����"
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator() || sunnyAdmins.contains(member.id.toString())){
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
    "serverInfo", "server", "zt", "������״̬", "״̬", "������",
    description = "������״̬��ѯ"
) {
    const val url = "http://manghui.cc/tools/r-get.php"
    const val roselleUrl = "https://mc.iroselle.com/api/data/getServerInfo"
    
    @Handler
    suspend fun CommandSender.handle() {
        if(user == null)
            return
        if(user !is Member)
            return
        val member = user as Member
        val group = member.group
        val groupId = group.id
        if(!sGroupMap.containsKey(groupId)) {
            sGroupMap[groupId] = SGroup(groupId)
        }
        val sGroup = sGroupMap[groupId]!!

        if(sGroup.roselleServerIp != "") {
            val ip = sGroup.roselleServerIp
            val result = SRequest(roselleUrl).roselleResult(ip, 0)
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
                    "��ѯ��ʱ: ${result.run_time}s"
            )
        }

        else if(sGroup.serverIp != "") {
            val ip = sGroup.serverIp
            val result = SRequest(url).result(ip)
            val args = result.split("<br>")
            var str = args[1]
            for(i in 2..7){
                str += "\n"
                str += args[i]
            }
            
            group.sendMsg("������״̬��ѯ", str)
        }

        else sendMessage("""
                ��Ⱥ��δ�󶨷�����
                ������ "/ip ������IP" �԰󶨷�����
            """.trimIndent())
        
    }
}

object SCIpBind: SimpleCommand(
    PluginMain,
    "ipBind", "ip", "��������", "��",
    description = "������״̬��ѯIP��"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        if(user !=null && user is Member){
            val member = user as Member
            val groupId = member.group.id
            if(!sGroupMap.containsKey(groupId)) {
                sGroupMap[groupId] = SGroup(groupId)
            }
            val sGroup = sGroupMap[groupId]!!
            
            val roselleResult = SRequest(SCServerInfo.roselleUrl).roselleResult(serverIp, 0)
            if(roselleResult.code == 1){
                sGroup.roselleServerIp = serverIp
                sGroup.serverIp = ""
                sendMessage("$serverIp �󶨳ɹ���")
                return
            }
            
            val result = SRequest(SCServerInfo.url).result(serverIp)
            if(!result.contains("�޷����Ӹ÷�����")){
                sGroup.serverIp = serverIp
                sGroup.roselleServerIp = ""
                sendMessage("$serverIp �󶨳ɹ���")
                return
            }
        }
        
        sendMessage("��ʧ��= =\n" +
            "��ȷ��������IP��ȷ��")
    }
}

object SCLaTeX: SimpleCommand(
    PluginMain,
    "LaTeX", "lx",
    description = "LaTeX��Ⱦ"
) {
    @Handler
    suspend fun CommandSender.group(text: String) {
        val contact = this.subject ?: return
        val bimg = SLaTeX.generate(text)
        val image = bimg.upload(contact)
        contact.sendMsg("LaTeX", image)
    }
}
