package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import io.github.sunshinewzy.sunnybot.utils.SServerPing
import io.github.sunshinewzy.sunnybot.utils.SServerPing.pingServer
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registeredCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

/**
 * Sunny Simple Commands
 */

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun regSSimpleCommands() {
    //ָ��ע��
    //Ĭ��m*Ϊ����ȺԱ u*Ϊ�����û�
    SCMenu.reg("u*")
    SCGameMenu.reg()
    SCInfo.reg("u*")
    SCAntiRecall.reg()
    SCIpBind.reg()
    SCIpBindPing.reg()
    SCJavaDoc.reg("u*")
    SCRepeater.reg()
    SCBingPicture.reg()
    SCWeather.reg("u*")
    
    //Debug
    SCDebugServerInfo.reg("console")
    SCDebugIntroduction.reg("console")
}


object SCMenu: SimpleCommand(
    PluginMain,
    "Menu", "cd", "�˵�", "����",
    description = "�˵�|�����б�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        var text = "===============\n"
        PluginMain.registeredCommands.forEach { 
            if(it.usage.contains("Debug")) return@forEach
            
            text += "�� ${it.usage.replaceFirst("\n", "")}\n"
            
            it.secondaryNames.forEach { seName ->
                text += "/$seName  "
            }
            text += "\n\n"
        }
        text += "===============\n"
        
        subject?.sendMsg("�˵� | �����б�", text)
    }
}

object SCGameMenu: SimpleCommand(
    PluginMain,
    "GameMenu", "game", "��Ϸ", "��Ϸ�˵�",
    description = "��Ϸ�˵�"
) {
    @Handler
    suspend fun CommandSender.handle() {
        subject?.sendMsg("��Ϸ�˵�", """
            ===============
            �� 24��
            �� ������
            �� Χ��
            ===============
            ������  #��������  �Կ�ʼ
            [��] #24��
        """.trimIndent()
        )
    }
}

object SCInfo: SimpleCommand(
    PluginMain,
    "��Ϣ", "info",
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
            sendMessage(PlainText("[Sunshine Technology Dollar]\n") + At(member) + 
                PlainText("����STD���Ϊ: ${sPlayer.std}"))
            return
        }
        sendMessage("[Sunshine Technology Dollar]\n" +
            "����STD���Ϊ: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "AntiRecall", "atrc", "������",
    description = "����/�رշ�����"
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator() || member.isSunnyAdmin()){
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

object SCDebugServerInfo: SimpleCommand(
    PluginMain,
    "DebugServerInfo", "dServer", "dzt",
    description = "Debug ������״̬��ѯ"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        val contact = sunnyBot?.getGroup(423179929L) ?: return
        
        val roselleResult = SRequest(SCServerInfo.roselleUrl).resultRoselle(serverIp, 0)
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

        sendMessage(contact.pingServer(serverIp))
    }
}

object SCIpBind: SimpleCommand(
    PluginMain,
    "IpBind", "ip", "��������", "��",
    description = "������״̬��ѯIP��"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        if(user !=null && user is Member){
            val member = user as Member
            val group = member.group
            val groupId = group.id
            val sGroup = sGroupMap[groupId] ?: return
            
            val roselleResult = SRequest(SCServerInfo.roselleUrl).resultRoselle(serverIp, 0)
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

object SCIpBindPing: SimpleCommand(
    PluginMain,
    "IpBindPing", "ipp", "��������Ping", "��P",
    description = "������״̬��ѯServerPing IP��"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        if(user !=null && user is Member){
            val member = user as Member
            val group = member.group
            val groupId = group.id
            val sGroup = sGroupMap[groupId] ?: return

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
        OI Wiki: https://oi-wiki.org/
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

object SCRepeater : SimpleCommand(
    PluginMain,
    "Repeater", "rep", "����",
    description = "����/�ر� ����"
) {
    @Handler
    suspend fun MemberCommandSender.handle(isRepeat: String) {
        val rep = isRepeat.toLowerCase()
        val sGroup = group.getSGroup()
        
        if(!user.isOperator() && !user.isSunnyAdmin()){
            sendMessage(At(user).plus(PlainText("������Ⱥ�������Ա��û������/�رո������ܵ�Ȩ�ޣ�")))
            group.sendMsg("����", "Ⱥ����״̬: ${sGroup.isRepeat}")
            return
        }
        
        if(rep.contains("t") || rep.contains("��")){
            sGroup.isRepeat = true
            group.sendMsg("����", "�����ѿ�����")
        }
        else if(rep.contains("f") || rep.contains("��")){
            sGroup.isRepeat = false
            group.sendMsg("����", "�����ѹرգ�")
        }
        else{
            group.sendMsg("����", "Ⱥ����״̬: ${sGroup.isRepeat}")
        }
    }
}

object SCBingPicture : SimpleCommand(
    PluginMain,
    "BingPicture", "bp", "ÿ��һͼ",
    description = "Bing��Ӧÿ��һͼ"
) {
    @Handler
    suspend fun CommandSender.handle() {
        val contact = subject ?: return
        val image = SRequest("https://api.yimian.xyz/img?type=wallpaper").resultImage(contact) ?: kotlin.run { 
            contact.sendMsg(description, "ͼƬ��ȡʧ��...")
            return
        }
        
        contact.sendMsg(description, image)
    }
}

object SCDebugIntroduction : SimpleCommand(
    PluginMain,
    "DebugIntroduction", "di", "���ҽ���",
    description = "Debug �������ҽ���"
) {
    @Handler
    suspend fun CommandSender.handle(groupId: Long) {
        val group = sunnyBot?.getGroup(groupId) ?: kotlin.run { 
            PluginMain.logger.warning("Ⱥ$groupId ��ȡʧ��")
            return
        }
        
        group.sendIntroduction()
    }
}

object SCWeather : SimpleCommand(
    PluginMain,
    "Weather", "����",
    description = "��ѯ����"
) {
    @Handler
    suspend fun CommandSender.handle() {
        val formatter = SimpleDateFormat("yyyy��MM��dd��")
        val date = formatter.format(Date(System.currentTimeMillis()))
        
        val msg = LightApp("""
            {"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����"}
        """.trimIndent())
//        {"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"$city","date":"$date","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
        
//        val msg = """
//            mirai:app:{"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"����","date":"1��30�� ����","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
//        """.trimIndent().parseMiraiCode()
        
        sendMessage(msg)
    }
}

/*
[mirai:source:51993,803246295][[Ӧ��]����]��ʹ�����°汾�ֻ�QQ�鿴
[mirai:app:{"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1",
"prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"����","date":"1��30�� ����","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}]
*/

