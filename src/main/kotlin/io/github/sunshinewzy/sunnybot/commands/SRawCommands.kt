package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.enums.SunSTSymbol
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.utils.SLaTeX.laTeXImage
import io.github.sunshinewzy.sunnybot.utils.SServerPing
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildXmlMessage

/**
 * Sunny Raw Commands
 */

suspend fun regSRawCommands() {
    //ָ��ע��
    //Ĭ��m*Ϊ����ȺԱ u*Ϊ�����û�
    SCLaTeX.reg("u*")
    SCDailySignIn.reg("u*")
    SCServerInfo.reg("u*")
    SCXmlMessage.reg("u*")

    //Debug
    SCDebugLaTeX.reg("console")
}


object SCLaTeX: RawCommand(
    PluginMain,
    "LaTeX", "lx",
    usage = "LaTeX��Ⱦ" usageWith "/lx LaTeX�ı�(�����пո�)"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        
        val text = args.contentToString()
        val image = contact.laTeXImage(text)
        contact.sendMsg("LaTeX", image)
    }
}

object SCDebugLaTeX: RawCommand(
    PluginMain,
    "debugLaTeX", "dlx",
    usage = "Debug LaTeX" usageWith "/dlx [g Ⱥ��] LaTeX�ı�(�����пո�)"
) {
    private const val groupIdSunST = 423179929L

    override suspend fun CommandSender.onCommand(args: MessageChain) {
        if(args.isEmpty())
            return
        
        var text = ""
        var groupId = groupIdSunST
        if(args.size >= 3 && args[0].contentToString() == "g") {
            groupId = args[1].contentToString().toLong()
            for(i in 2 until args.size){
                text += args[i].contentToString()
            }
        }
        else text = args.contentToString()

        val group = miraiBot?.getGroup(groupId) ?: return
        val image = group.laTeXImage(text)
        group.sendMsg("LaTeX", image)
    }
}

object SCDailySignIn: RawCommand(
    PluginMain,
    "dailySignIn", "qd", "ǩ��", "��",
    usage = "ÿ��ǩ��" usageWith "/ǩ�� <���Ľ�������>"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val member = user ?: return
        
        if(member !is Member){
            sendMessage("��ֻ����Ⱥ��ǩ����")
            return
        }
        
        val sPlayer = member.getSPlayer()
        val group = member.group
        val dailySignIns = group.getSGroup().dailySignIns
        if(sPlayer.isDailySignIn){
            var ans = " �������Ѿ�ǩ�����ˣ������ظ�ǩ����\n"
            dailySignIns.forEach { 
                if(it.first == member.id){
                    ans += "���Ľ�������:\n" + it.second
                    return@forEach
                }
            }
            
            sendMessage(At(member) + PlainText(ans))
            return
        }
        
        if(args.isEmpty() || args[0].contentToString() == ""){
            sendMessage("""
                $name ������ "/ǩ�� <���Ľ�������>" ��ǩ��
                (ÿ��ǩ��ǰ5��TA�����ԻᱻչʾŶ~)
            """.trimIndent())
            return
        }
        
        val arg = args.contentToString().replace("[|]".toRegex(), "")
            .replace("\'", "")
            .newSunSTSymbol(SunSTSymbol.ENTER)
        dailySignIns.add(member.id to arg)
        
        if(dailySignIns.size < 5)
            group.sendMessage(At(member) + " ���Ǳ�Ⱥ�����${dailySignIns.size}��ǩ���ģ����������ѱ�����չʾ�б�")
        else
            group.sendMessage(At(member) + " ���Ǳ�Ⱥ�����${dailySignIns.size}��ǩ���ģ�ף��RP++ ��")
        
        sPlayer.isDailySignIn = true
        
        member.addSTD(5)
        var msg = """
            ǩ���ɹ���STD +5 !
            (���Ǳ�Ⱥ�����${dailySignIns.size}��ǩ����)
            
            <���ձ�Ⱥǩ��ǰ5>
            
        """.trimIndent()
        for(i in 0 until dailySignIns.size){
            val signIn = dailySignIns[i]
            msg += "${i + 1}. ${group[signIn.first].nameCard}: " + signIn.second.oldSunSTSymbol(SunSTSymbol.ENTER) + "\n"
        }
        group.sendMsg("ÿ��ǩ��", At(member) + " $msg")
    }
}

object SCServerInfo: RawCommand(
    PluginMain,
    "serverInfo", "server", "zt", "������״̬", "״̬", "������",
    usage = "������״̬��ѯ" usageWith """
        /zt         Ĭ�ϲ�ѯ��ʽ
        /zt 1       ǿ��ʹ��Ping��ѯ
        /zt 2       ǿ��ʹ�������Ʋ�ѯ
        /zt m       ��ʾ��ϸmod��Ϣ
    """.trimIndent()
) {
    const val roselleUrl = "https://mc.iroselle.com/api/data/getServerInfo"

    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val str = args.contentToString()
        
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        val groupId = group.id
        val sGroup = SSaveGroup.sGroupMap[groupId] ?: return

        if(str == "2" || sGroup.roselleServerIp != "") {
            val ip = if(sGroup.roselleServerIp != "") sGroup.roselleServerIp else sGroup.serverIp
            val result = SRequest(roselleUrl).resultRoselle(ip, 0)
            val res = result.res

            var serverStatus = "����"
            if(res.server_status == 1)
                serverStatus = "����"

            group.sendMsg("������״̬��ѯ - ������",
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

        else if((str == "1" || str.contains("m") || str == "" || args.isEmpty()) && (sGroup.serverIp != "" || sGroup.roselleServerIp != "")) {
            val ip = if(sGroup.serverIp != "") sGroup.serverIp else sGroup.roselleServerIp

            group.sendMsg("������״̬��ѯ - Ping", SServerPing.pingServer(ip, str.contains("m")))
        }

        else sendMessage("""
                ��Ⱥ��δ�󶨷�����
                ������ "/ip ������IP" �԰󶨷�����
            """.trimIndent())
    }
}

object SCXmlMessage: RawCommand(
    PluginMain,
    "xmlMessage", "xml",
    usage = "����һ��Xml��Ϣ" usageWith "/xml <��Ϣ����>"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        
        val str = args.contentToString()
        
        val msg = buildXmlMessage(1) {
            item { 
                title(str)
//                picture("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3078106429,1249965398&fm=26&gp=0.jpg")
            }
                
//            source("SunST", "https://www.mcbbs.net/thread-1015897-1-1.html")
//            
//            actionData = "https://www.mcbbs.net/thread-1015897-1-1.html"
        }
        
        contact.sendMessage(msg.contentToString())
        contact.sendMessage(msg)
    }
}