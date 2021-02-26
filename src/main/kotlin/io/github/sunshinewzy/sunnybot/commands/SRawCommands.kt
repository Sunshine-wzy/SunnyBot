package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.enums.SunSTSymbol
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.utils.SLaTeX.laTeXImage
import io.github.sunshinewzy.sunnybot.utils.SServerPing
import io.github.sunshinewzy.sunnybot.utils.SServerPing.pingServer
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * Sunny Raw Commands
 */

@ExperimentalCommandDescriptors
@ConsoleExperimentalApi
suspend fun regSRawCommands() {
    //ָ��ע��
    //Ĭ��m*Ϊ����ȺԱ u*Ϊ�����û�
    SCLaTeX.reg("u*")
    SCDailySignIn.reg("u*")
    SCServerInfo.reg("u*")
    SCXmlMessage.reg("u*")
    SCRedEnvelopes.reg()
    SCRandomImage.reg("u*")
    SCWords.reg("u*")

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
        val image = contact.laTeXImage(text) ?: return
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

        val group = sunnyBot?.getGroup(groupId) ?: return
        val image = group.laTeXImage(text) ?: return
        group.sendMsg("LaTeX", image)
    }
}

object SCDailySignIn: RawCommand(
    PluginMain,
    "DailySignIn", "qd", "ǩ��", "��",
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
        if(arg.length > 20){
            sendMessage(At(member) + " ��������̫���ˣ���������20������")
            return
        }
        
        
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
        
        val last = if(dailySignIns.size < 5) dailySignIns.size else 5
        for(i in 0 until last){
            val signIn = dailySignIns[i]
            msg += "${i + 1}. ${group[signIn.first]?.nameCard}: " + signIn.second.oldSunSTSymbol(SunSTSymbol.ENTER) + "\n"
        }
        group.sendMsg("ÿ��ǩ��", At(member) + " $msg")
    }
}

object SCServerInfo: RawCommand(
    PluginMain,
    "ServerInfo", "server", "zt", "������״̬", "״̬", "������",
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

        else if((str == "1" || str == "m" || str == "" || args.isEmpty()) && (sGroup.serverIp != "" || sGroup.roselleServerIp != "")) {
            val ip = if(sGroup.serverIp != "") sGroup.serverIp else sGroup.roselleServerIp

            group.sendMsg("������״̬��ѯ - Ping", group.pingServer(ip, str.contains("m")))
        }
        
        else if(str != ""){
            if(SServerPing.checkServer(str))
                group.sendMsg("������״̬��ѯ - Ping", group.pingServer(str, true))
            else group.sendMsg("������״̬��ѯ - Ping", "��ѯʧ��= =\n" +
                "��ȷ��������IP��ȷ�ҵ�ǰ���������ߣ�")
        }

        else sendMessage("""
                ��Ⱥ��δ�󶨷�����
                ������ "/ip ������IP" �԰󶨷�����
            """.trimIndent())
    }
}

object SCXmlMessage: RawCommand(
    PluginMain,
    "XmlMessage", "xml",
    usage = "����һ��Xml��Ϣ" usageWith "/xml <��Ϣ����>"
) {
    @MiraiExperimentalApi
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        
        val text = args.contentToString()
        
        val msg = buildXmlMessage(1) {
            item { 
                layout = 2
                
                title("[SkyDream]��֮��")
                summary(text)
                
                picture("https://s3.ax1x.com/2021/01/30/yFFwod.png")
            }
            
            source("Sunshine Technology")
            
            serviceId = 1
            action = "web"
            url = "https://www.mcbbs.net/thread-1015897-1-1.html"
            brief = "Sky Dream"
        
            templateId = 123
        }
        
        contact.sendMessage(msg)
    }
}

/*
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<msg serviceID="60" templateID="123" action="web" brief="���ѱ��Ƴ���Ⱥ" sourceMsgId="0" url="" flag="0" adverSign="0" multiMsgFlag="0">
<item layout="1" advertiser_id="0" aid="0" />
<item layout="1" advertiser_id="0" aid="0">
<summary size="��FF0000">Ⱥ���ٻ�����</summary></item>
<source name="" icon="" action="" appid="-1" /></msg>
*/

object SCRedEnvelopes: RawCommand(
    PluginMain,
    "RedEnvelopes", "re", "���",
    usage = "����һ�������Ϣ" usageWith "/��� <�������>"
) {
    @MiraiExperimentalApi
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = user ?: return
        if(contact !is Member) return

        var id = 1
        var text = args.contentToString()
        if(contact.isOperator() || contact.isSunnyAdmin()){
            if(args.size > 1){
                val firstArg = args[0]
                if(firstArg is PlainText){
                    val firstNum = firstArg.contentToString().toInt()
                    if(firstNum in 1..100){
                        id = firstNum
                        text = ""
                        args.forEach { 
                            text += it.contentToString()
                        }
                    }
                }
            }
        }

        val msg = buildXmlMessage(id) {
            item {
                layout = 2

                title("QQ���")
                summary(text)

                picture("https://s3.ax1x.com/2021/02/11/yB4uFA.png")
            }

            source("QQ���")

            serviceId = id
            action = "web"
            url = "https://oi-wiki.org"
            brief = "[QQ���]��ϲ����"

            templateId = 123
        }
        
//        val msg = """
//            [mirai:app:{"app":"com.tencent.miniapp","desc":"","view":"all","ver":"1.0.0.89","prompt":"[QQ���]��ϲ����","meta":{"all":{"preview":"http://gchat.qpic.cn/gchatpic_new/3584906133/956021029-2885039703-7B5004A5ED0FCF042BF5AF737EA1762B/0?term=2","title":"","buttons":[{"name":"�޲��׼����","action":"http://www.qq.com"}],"jumpUrl":"","summary":"\n������һ�� <�޲��׼����>  ����ʹ���ĸ��汾���ֻ�QQ�����ܲ��պ��  ��Ϊ�޲��׼��Ĺ�ʵ���ܿ�����ʩ��  �ǿ��Լ���ȡ�ģ�\n"}},"config":{"forward":true}}]
//        """.trimIndent().deserializeMiraiCode()

        subject?.sendMessage(msg)
    }
}

object SCRandomImage : RawCommand(
    PluginMain,
    "RandomImage", "ri", "���ͼƬ", "ͼƬ",
    description = "���ͼƬ",
    usage = "���ͼƬ"
) {
    private const val url = "https://api.yimian.xyz/img"
    private val params = hashMapOf(
        "moe" to "����ԪͼƬ",
        "wallpaper" to "Bing��ֽ",
        "head" to "����Ԫͷ��",
        "imgbed" to "ͼ��ͼƬ",
        "moe&size=1920x1080" to "1920x1080�ߴ����ԪͼƬ"
    )


    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val plainText = args.findIsInstance<PlainText>()
        
        var img: Image? = null
        if(plainText == null || plainText.content == ""){
            img = SRequest(url).resultImage(contact)
        }
        else{
            val text = plainText.content
            if(params.containsKey(text)){
                img = SRequest("$url?type=$text").resultImage(contact)
            }
            else{
                var res = "��������ȷ��\n���������²���֮һ:\n"
                params.forEach { (key, value) ->
                    res += "$key  -  $value\n"
                }
                contact.sendMsg(description, res)
            }
        }

        
        if(img == null){
            contact.sendMsg(description, "ͼƬ��ȡʧ��...")
            return
        }
        
        contact.sendMsg(description, img)
    }

}

object SCWords : RawCommand(
    PluginMain,
    "Words", "yy", "һ��",
    description = "һ��",
    usage = "һ��"
) {
    private const val url = "https://api.yimian.xyz/words/"
    private val params = hashMapOf(
        "en" to "Ӣ��",
        "zh" to "����"
    )


    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val plainText = args.findIsInstance<PlainText>()

        var words: String? = null
        if(plainText == null || plainText.content == ""){
            words = SRequest(url).result()
        }
        else{
            val text = plainText.content
            if(params.contains(text)){
                words = SRequest("$url?lang=$text").result()
            }
            else{
                var res = "��������ȷ��\n���������²���֮һ:\n"
                params.forEach { (key, value) ->
                    res += "$key  -  $value\n"
                }
                contact.sendMsg(description, res)
            }
        }


        if(words == null){
            contact.sendMsg(description, "һ�Ի�ȡʧ��...")
            return
        }

        contact.sendMsg(description, words)
    }

}