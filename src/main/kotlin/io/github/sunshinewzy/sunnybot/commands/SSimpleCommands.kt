package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import io.github.sunshinewzy.sunnybot.utils.SServerPing
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

/**
 * Sunny Simple Commands
 */

suspend fun regSSimpleCommands() {
    //ָ��ע��
    //Ĭ��m*Ϊ����ȺԱ u*Ϊ�����û�
    SCMenu.reg()
    SCInfo.reg("u*")
    SCAntiRecall.reg()
    SCServerInfo.reg("u*")
    SCIpBind.reg()
    SCJavaDoc.reg("u*")
    
    //Debug
    SCDebugServerInfo.reg("console")
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
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        val groupId = group.id
        val sGroup = sGroupMap[groupId] ?: return

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
            
            group.sendMsg("������״̬��ѯ", SServerPing.pingServer(ip))
            
        }

        else sendMessage("""
                ��Ⱥ��δ�󶨷�����
                ������ "/ip ������IP" �԰󶨷�����
            """.trimIndent())
        
    }
}

object SCDebugServerInfo: SimpleCommand(
    PluginMain,
    "DebugServerInfo", "dServer", "dzt",
    description = "Debug ������״̬��ѯ"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        val contact = miraiBot?.getGroup(423179929L) ?: return
        
        val roselleResult = SRequest(SCServerInfo.roselleUrl).roselleResult(serverIp, 0)
        if(roselleResult.code == 1){
            val res = roselleResult.res
            var serverStatus = "����"
            if(res.server_status == 1)
                serverStatus = "����"

            sendMessage(
                "\t�� SunnyBot ��\n" +
                    "������IP: $serverIp\n" +
                    "������״̬: $serverStatus\n" +
                    "��ǰ���������: ${res.server_player_online}\n" +
                    "�����������: ${res.server_player_max}\n" +
                    "�վ���������: ${res.server_player_average}\n" +
                    "��ʷ���ͬʱ��������: ${res.server_player_history_max}\n" +
                    "����ƽ����������: ${res.server_player_yesterday_average}\n" +
                    "�������ͬʱ��������: ${res.server_player_yesterday_max}\n" +
                    "����ʱ��: ${res.update_time}\n" +
                    "��ѯ��ʱ: ${roselleResult.run_time}s"
            )
            return
        }

        sendMessage(SServerPing.pingServer(serverIp))
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
            val group = member.group
            val groupId = group.id
            val sGroup = sGroupMap[groupId] ?: return
            
            val roselleResult = SRequest(SCServerInfo.roselleUrl).roselleResult(serverIp, 0)
            if(roselleResult.code == 1){
                sGroup.roselleServerIp = serverIp
                sGroup.serverIp = ""
                sendMessage("$serverIp �󶨳ɹ���")
                return
            }
            
            if(SServerPing.checkServer(serverIp)){
                sGroup.serverIp = serverIp
                sGroup.roselleServerIp = ""
                sendMessage("$serverIp �󶨳ɹ���")
                return
            }
        }
        
        sendMessage("��ʧ��= =\n" +
            "��ȷ��������IP��ȷ�ҵ�ǰ���������ߣ�")
    }
}

object SCJavaDoc: SimpleCommand(
    PluginMain,
    "JavaDoc", "jd",
    description = "�鿴����JavaDoc"
) {
    private val javaDocs = """
        Java8: https://docs.oracle.com/javase/8/docs/api/overview-summary.html 
        
        Bukkit�̳�:
        ���� https://alpha.tdiant.net/
        ���� https://bdn.tdiant.net/
        
        BukkitAPI - Javadoc: 
        1.7.10��(�ѹ�ʱ):https://jd.bukkit.org/ 
        Chinese_Bukkit: 
        1.12.2��:http://docs.zoyn.top/bukkitapi/1.12.2/ 
        1.13+��:https://bukkit.windit.net/javadoc/ 
        Spigot: https://hub.spigotmc.org/javadocs/spigot/ 
        Paper: https://papermc.io/javadocs/paper/
        
        Sponge: https://docs.spongepowered.org/stable/zh-CN/
        BungeeCord:
        API: https://ci.md-5.net/job/BungeeCord/ws/api/target/apidocs/overview-summary.html
        API-Chat: https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/overview-summary.html
        MCP Query: https://mcp.exz.me/
        Vault: https://pluginwiki.github.io/VaultAPI/
        ProtocolLib: https://ci.dmulloy2.net/job/ProtocolLib/javadoc/
    """.trimIndent()
    
    @Handler
    suspend fun CommandSender.handle() {
        val contact = subject ?: return
        contact.sendMsg("JavaDoc", javaDocs)
    }
}
