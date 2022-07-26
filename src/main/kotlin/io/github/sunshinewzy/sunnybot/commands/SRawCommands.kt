package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_3
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_MEMBER
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_USER
import io.github.sunshinewzy.sunnybot.enums.ServerType
import io.github.sunshinewzy.sunnybot.enums.SunSTSymbol
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.timer.STimer
import io.github.sunshinewzy.sunnybot.utils.RconManager
import io.github.sunshinewzy.sunnybot.utils.SLaTeX.laTeXImage
import io.github.sunshinewzy.sunnybot.utils.SServerPing
import io.github.sunshinewzy.sunnybot.utils.SServerPing.pingServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sunny Raw Commands
 */

fun regSRawCommands() {
    //指令注册

    SCLaTeX.register()
    SCDailySignIn.register()
    SCServerInfo.register()
    SCIpBind.register()
    SCXmlMessage.register()
    SCRandomImage.register()
    SCWords.register()
    SCSound.register()
    SCGroupManager.register()
    SCMiraiCode.register()
    SCMoeImage.register()
    SCGaoKaoCountDown.register()
    SCReminder.register()
    SCRcon.register()
    SCRconRun.register()
    
    //默认m*为任意群员 u*为任意用户
//    SCLaTeX.reg("u*")
//    SCDailySignIn.reg("u*")
//    SCServerInfo.reg("u*")
//    SCIpBind.reg()
//    SCXmlMessage.reg("u*")
//    SCRandomImage.reg("u*")
//    SCWords.reg("u*")
//    SCSound.reg("u*")
//    SCGroupManager.reg()
//    SCMiraiCode.reg("u*")
//    SCMoeImage.reg("u*")

    //Debug
    SCDebugLaTeX.reg("console")
}


object SCLaTeX: RawCommand(
    PluginMain,
    "LaTeX", "lx",
    usage = "LaTeX渲染" usageWith "/lx LaTeX文本(可以有空格)",
    parentPermission = PERM_EXE_USER
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val text = args.contentToString()
        var flag = false
        val msg = MessageChainBuilder()
        var txt = ""
        var laTex = ""
        
        text.forEach { 
            if(it == '$') {
                if(flag) {
                    flag = false
                    contact.laTeXImage(laTex)?.let { img -> msg.add(img) }
                    laTex = ""
                } else {
                    flag = true
                    msg.add(txt)
                    txt = ""
                }
            } else {
                if(flag) {
                    laTex += it
                } else {
                    txt += it
                }
            }
        }
        
        if(txt != "") msg.add(txt)
        contact.sendMsg("LaTeX", msg.asMessageChain())
    }
}

object SCDebugLaTeX: RawCommand(
    PluginMain,
    "debugLaTeX", "dlx",
    usage = "Debug LaTeX" usageWith "/dlx [g 群号] LaTeX文本(可以有空格)"
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

        val group = sunnyBot.getGroup(groupId) ?: return
        val image = group.laTeXImage(text) ?: return
        group.sendMsg("LaTeX", image)
    }
}

object SCDailySignIn: RawCommand(
    PluginMain,
    "DailySignIn", "qd", "签到", "打卡",
    usage = "每日签到" usageWith "/签到 <您的今日赠言>",
    parentPermission = PERM_EXE_USER
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val member = user ?: return
        
        if(member !is Member){
            sendMessage("您只能在群中签到！")
            return
        }
        
        val sPlayer = member.getSPlayer()
        val group = member.group
        val dailySignIns = group.getSGroup().dailySignIns
        if(sPlayer.isDailySignIn){
            var ans = " 您今天已经签到过了，不能重复签到！\n"
            dailySignIns.forEach { 
                if(it.first == member.id){
                    ans += "您的今日赠言:\n" + it.second
                    return@forEach
                }
            }
            
            sendMessage(At(member) + PlainText(ans))
            return
        }
        
        if(args.isEmpty() || args[0].contentToString() == ""){
            sendMessage("""
                $name 请输入 "/签到 <您的今日赠言>" 以签到
                (每日签到前5和TA的赠言会被展示哦~)
            """.trimIndent())
            return
        }
        
        val arg = args.contentToString().replace("[|]".toRegex(), "")
            .replace("\'", "")
            .newSunSTSymbol(SunSTSymbol.ENTER)
        if(arg.length > 20){
            sendMessage(At(member) + " 您的赠言太长了，请限制在20字以内")
            return
        }
        
        
        dailySignIns.add(member.id to arg)
        
        if(dailySignIns.size < 5)
            group.sendMessage(At(member) + " 您是本群今天第${dailySignIns.size}个签到的，您的赠言已被列入展示列表！")
        else
            group.sendMessage(At(member) + " 您是本群今天第${dailySignIns.size}个签到的，祝您RP++ ！")
        
        sPlayer.isDailySignIn = true
        
        member.addSTD(5)
        var msg = """
            签到成功，STD +5 !
            (您是本群今天第${dailySignIns.size}个签到的)
            
            <今日本群签到前5>
            
        """.trimIndent()
        
        val last = if(dailySignIns.size < 5) dailySignIns.size else 5
        for(i in 0 until last){
            val signIn = dailySignIns[i]
            msg += "${i + 1}. ${group[signIn.first]?.nameCard}: " + signIn.second.oldSunSTSymbol(SunSTSymbol.ENTER) + "\n"
        }
        group.sendMsg("每日签到", At(member) + " $msg")
    }
}

object SCServerInfo: RawCommand(
    PluginMain,
    "ServerInfo", "server", "zt", "服务器状态", "状态", "服务器",
    description = "服务器状态查询",
    usage = "服务器状态查询" usageWith """
        /zt         默认查询方式
        /zt m       显示详细mod信息
        /zt [IP代号] 根据代号绑定的IP查询
        /zt [ip]    根据IP查询
    """.trimIndent(),
    parentPermission = PERM_EXE_USER
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

//        if(str == "2") {
//            val ip = sGroup.serverIp
//            val result = SRequest(roselleUrl).resultRoselle(ip, 0)
//            val res = result.res
//
//            var serverStatus = "离线"
//            if(res.server_status == 1)
//                serverStatus = "在线"
//
//            group.sendMsg("服务器状态查询 - 洛神云",
//                    "服务器IP: $ip\n" +
//                    "服务器状态: $serverStatus\n" +
//                    "当前在线玩家数: ${res.server_player_online}\n" +
//                    "在线玩家上限: ${res.server_player_max}\n" +
//                    "日均在线人数: ${res.server_player_average}\n" +
//                    "历史最高同时在线人数: ${res.server_player_history_max}\n" +
//                    "昨日平均在线人数: ${res.server_player_yesterday_average}\n" +
//                    "昨日最高同时在线人数: ${res.server_player_yesterday_max}\n" +
//                    "更新时间: ${res.update_time}\n" +
//                    "查询用时: ${result.run_time}s"
//            )
//        }

        if(sGroup.serverIps.containsKey(str)) {
            sGroup.serverIps[str]?.let {
                group.sendMsg(description, group.pingServer(it.second, it.first))
            }
        }
        
        else if((str == "m" || str == "" || args.isEmpty()) && sGroup.serverIp.first != ServerType.NOT)
            group.sendMsg(description, group.pingServer(sGroup.serverIp.second, sGroup.serverIp.first, str.contains("m")))
        
        else if(str != ""){
            val check = SServerPing.checkServer(str)
            if(check != ServerType.NOT)
                group.sendMsg(description, group.pingServer(str, check, true))
            else group.sendMsg(description, "查询失败= =\n" +
                "请确保服务器IP正确且当前服务器在线！")
        }

        else sendMessage("""
                本群还未绑定服务器
                请输入 "/ip 服务器IP" 以绑定服务器
            """.trimIndent())
    }
}

object SCIpBind: RawCommand(
    PluginMain,
    "IpBind", "ip", "服务器绑定", "绑定",
    description = "服务器状态查询IP绑定",
    usage = "服务器状态查询IP绑定",
    parentPermission = PERM_EXE_MEMBER
) {

    override suspend fun CommandSender.onCommand(args: MessageChain) {
        if(user != null && user is Member){
            val member = user as Member
            val group = member.group
            val groupId = group.id
            val sGroup = SSaveGroup.sGroupMap[groupId] ?: return

            
            processSCommand(args) {
                "set" {
                    any { list ->
                        val symbol = list.first
                        if(symbol.isLetterDigitOrChinese()) {
                            if(list.size >= 2) {
                                val ip = list[1]
                                val check = SServerPing.checkServer(ip)
                                if(check != ServerType.NOT){
                                    sGroup.serverIps[symbol] = check to ip
                                    sendMsg("$ip ($check) 绑定成功！")
                                } else sendMsg(description, "绑定失败= =\n" +
                                    "请确保服务器IP正确且当前服务器在线！")
                            } else sendMsg(description, "请在 [IP代号] 后输入 [服务器IP]")
                            
                            
                        } else sendMsg(description, "IP的代号只能含有英文、数字、汉字，不能包含任何符号！")
                    }
                    
                    empty { 
                        sendMsg(description, """
                            请在set后输入: [绑定IP的代号] [服务器IP]
                            例: /ip set bd www.baidu.com
                            
                            Tips:
                            IP的代号只能含有英文、数字、汉字，不能包含任何符号！
                        """.trimIndent())
                    }
                }
                
                "remove" {
                    anyContents(false) { 
                        if(sGroup.serverIps.containsKey(it)) {
                            sGroup.serverIps.remove(it)
                            sendMsg(description, "IP代号 '$it' 删除成功~")
                        } else sendMsg(description, "IP代号 '$it' 不存在！")
                    }
                }
                
                "list" {
                    empty { 
                        if(sGroup.serverIps.isEmpty()) {
                            sendMsg(description, "本群未绑定任何IP代号！")
                            return@empty
                        }
                        
                        var symbolIps = "已绑定的所有IP代号:"
                        var index = 1
                        sGroup.serverIps.forEach { (symbol, pair) -> 
                            symbolIps += "\n$index. $symbol -> ${pair.second} (${pair.first})"
                            index++
                        }
                        
                        sendMsg(description, symbolIps)
                    }
                }
                
                anyContents(false) {
                    if(it.startsWith("set") || it.startsWith("remove") || it.startsWith("list"))
                        return@anyContents
                    
                    val check = SServerPing.checkServer(it)
                    if(check != ServerType.NOT){
                        sGroup.serverIp = check to it
                        sendMsg("$it ($check) 绑定成功！")
                    } else sendMsg(description, "绑定失败= =\n" +
                        "请确保服务器IP正确且当前服务器在线！")
                }
                
                empty {
                    sendMsg(
                        description, """
                            命令参数:
                            set     -   设置绑定IP代号
                            remove  -   移除IP代号
                            list    -   显示所有IP代号
                        """.trimIndent())
                }
            }
        }
        
    }
    
}


object SCXmlMessage: RawCommand(
    PluginMain,
    "XmlMessage", "xml",
    usage = "发送一条Xml消息" usageWith "/xml <消息内容>",
    parentPermission = PERM_EXE_USER
) {
    @MiraiExperimentalApi
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        
        val text = args.contentToString()
        
        val msg = buildXmlMessage(1) {
            item { 
                layout = 2
                
                title("[SkyDream]天之梦")
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
<msg serviceID="60" templateID="123" action="web" brief="您已被移出本群" sourceMsgId="0" url="" flag="0" adverSign="0" multiMsgFlag="0">
<item layout="1" advertiser_id="0" aid="0" />
<item layout="1" advertiser_id="0" aid="0">
<summary size="×FF0000">群友召唤术？</summary></item>
<source name="" icon="" action="" appid="-1" /></msg>
*/

object SCRedEnvelopes: RawCommand(
    PluginMain,
    "RedEnvelopes", "re", "红包",
    usage = "发送一个红包消息" usageWith "/红包 <红包内容>",
    parentPermission = PERM_EXE_USER
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

                title("QQ红包")
                summary(text)

                picture("https://s3.ax1x.com/2021/02/11/yB4uFA.png")
            }

            source("QQ红包")

            serviceId = id
            action = "web"
            url = "https://oi-wiki.org"
            brief = "[QQ红包]恭喜发财"

            templateId = 123
        }
        
//        val msg = """
//            [mirai:app:{"app":"com.tencent.miniapp","desc":"","view":"all","ver":"1.0.0.89","prompt":"[QQ红包]恭喜发财","meta":{"all":{"preview":"http://gchat.qpic.cn/gchatpic_new/3584906133/956021029-2885039703-7B5004A5ED0FCF042BF5AF737EA1762B/0?term=2","title":"","buttons":[{"name":"无产阶级红包","action":"http://www.qq.com"}],"jumpUrl":"","summary":"\n发了送一个 <无产阶级红包>  无论使用哪个版本的手机QQ均不能查收红包  因为无产阶级的果实不能靠别人施舍  是靠自己争取的！\n"}},"config":{"forward":true}}]
//        """.trimIndent().deserializeMiraiCode()

        subject?.sendMessage(msg)
    }
}

object SCRandomImage : RawCommand(
    PluginMain,
    "RandomImage", "ri", "随机图片", "图片",
    description = "随机图片",
    usage = "随机图片",
    parentPermission = PERM_EXE_3
) {
    private const val url = "https://api.yimian.xyz/img"
    private val params = hashMapOf(
        "moe" to "二次元图片",
        "wallpaper" to "Bing壁纸",
        "head" to "二次元头像",
        "imgbed" to "图床图片",
        "moe&size=1920x1080" to "1920x1080尺寸二次元图片"
    )


    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val plainText = args.findIsInstance<PlainText>()
        
        GlobalScope.launch(SCoroutine.image) {
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
                    var res = "参数不正确！\n请输入以下参数之一:\n"
                    params.forEach { (key, value) ->
                        res += "$key  -  $value\n"
                    }
                    contact.sendMsg(description, res)
                }
            }


            if(img == null){
                contact.sendMsg(description, "图片获取失败...")
            } else contact.sendMsg(description, img)
        }
    }

}

object SCWords : RawCommand(
    PluginMain,
    "Words", "yy", "一言",
    description = "一言",
    usage = "一言",
    parentPermission = PERM_EXE_USER
) {
    private const val url = "https://api.yimian.xyz/words/"
    private val params = hashMapOf(
        "en" to "英语",
        "zh" to "中文"
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
                var res = "参数不正确！\n请输入以下参数之一:\n"
                params.forEach { (key, value) ->
                    res += "$key  -  $value\n"
                }
                contact.sendMsg(description, res)
            }
        }


        if(words == null){
            contact.sendMsg(description, "一言获取失败...")
            return
        }

        contact.sendMsg(description, words)
    }

}

object SCSound : RawCommand(
    PluginMain,
    "Sound", "snd", "语音",
    usage = "语音",
    parentPermission = PERM_EXE_USER
) {
    const val popularUrl = "https://api.meowpad.me/v2/sounds/popular?skip=0"
    const val downloadUrl = "https://api.meowpad.me/v1/download/"
    
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val arg = args.findIsInstance<PlainText>()?.content
        var text = ""
        
        val folder = File(PluginMain.dataFolder, "Voice")
        val files = folder.listFiles() ?: emptyArray()
        
        if(arg == null || arg == ""){
            files.forEachIndexed { i, sound ->
                val order = i + 1
                text += "$order. ${sound.nameWithoutExtension}\n"
            }
            
            text += "请输入 \"/snd 序号\" 获取对应的语音~"
        }
        else{
            if(arg.isInteger()){
                val order = arg.toInt()
                
                if(order - 1 in files.indices){
                    val sound = files[order - 1]
                    
                    contact.sendMessage(sound.toExternalResource().uploadAsVoice(contact))
                    text = "${sound.nameWithoutExtension} 奉上~"
                }
                else{
                    text = "获取失败！\n不存在序号 $order"
                }
            }
            else{
                text = "获取失败！\n参数只能为数字"
            }
        }
        
        contact.sendMsg("语音", text)
    }
    
}

object SCGroupManager: RawCommand(
    PluginMain,
    "GroupManager", "gm", "群管理",
    usage = "群管理", description = "群管理",
    parentPermission = PERM_EXE_MEMBER
) {
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val group = subject ?: return
        val member = user ?: return
        if(group !is Group || member !is Member){
            sendMsg(description, "群管理只能在群中使用！")
            return
        }
        if(!member.isOperator() && !member.isSunnyAdmin()){
            sendMsg(description, At(member) + " 您不是管理员，不能使用群管理功能！")
            return
        }
        
        processSCommand(args) {
            
            "join" {
                "set" {
                    any { list ->
                        var welcomeMsg = ""
                        list.forEach { 
                            welcomeMsg += "$it "
                        }
                        
                        group.getSGroup().welcomeMessage = welcomeMsg
                        sendMsg(description, "入群欢迎成功设置为:\n$welcomeMsg")
                    }
                }
                
                "remove" {
                    empty { 
                        group.getSGroup().welcomeMessage = ""
                        sendMsg(description, "入群欢迎移除成功！")
                    }
                }
                
                "show" {
                    empty { 
                        sendMsg(description, "当前入群欢迎为:\n${group.getSGroup().welcomeMessage}")
                    }
                }
                
                empty {
                    sendMsg(description, "请加上参数 [set/remove/show] 以 设置/移除/查看 入群欢迎")
                }
            }
            
            "leave" {
                "set" {
                    any { list ->
                        var leaveMsg = ""
                        list.forEach { 
                            leaveMsg += "$it "
                        }
                        
                        group.getSGroup().leaveMessage = leaveMsg
                        sendMsg(description, "退群提示成功设置为:\n$leaveMsg")
                    }
                }

                "remove" {
                    empty {
                        group.getSGroup().leaveMessage = ""
                        sendMsg(description, "退群提示移除成功！")
                    }
                }

                "show" {
                    empty {
                        sendMsg(description, "当前退群提示为:\n${group.getSGroup().leaveMessage}")
                    }
                }

                empty {
                    sendMsg(description, "请加上参数 [set/remove/show] 以 设置/移除/查看 退群提示")
                }
            }
            
            
            "apply" {
                "add" {
                    any { list ->
                        var str = ""
                        list.forEach { str += it }
                        group.getSGroup().autoApply += str
                        sendMsg(description, "关键字添加成功:\n$str")
                    }
                }

                "remove" {
                    any { list ->
                        val first = list.first
                        if(first.isInteger()){
                            val order = first.toInt()
                            val accList = group.getSGroup().autoApply

                            if((order - 1) in accList.indices){
                                sendMsg(description, "关键字 $order: ${accList[order - 1]}\n移除成功！")
                                accList.removeAt(order - 1)
                            }
                            else{
                                sendMsg(description, "移除失败，不存在序号为 $order 的关键字！")
                            }
                        }
                        else sendMsg(description, "序号只能为数字！")
                    }

                    empty {
                        var msg = """
                                请输入关键字的序号以移除该关键字
                                
                                加群审批-自动同意 关键字列表:
                            """.trimIndent()
                        group.getSGroup().autoApply.forEachIndexed { i, str ->
                            msg += "\n${i + 1}. $str"
                        }
                        sendMsg(description, msg)
                    }
                }

                "clear" {
                    group.getSGroup().autoApply.clear()
                    sendMsg(description, "加群审批-自动同意 关键字已清空~")
                }
                
                "list" {
                    var msg = "加群审批-自动同意 关键字列表:"
                    group.getSGroup().autoApply.forEachIndexed { i, str ->
                        msg += "\n${i + 1}. $str"
                    }
                    sendMsg(description, msg)
                }

                empty {
                    sendMsg(description, """
                            请加上参数 [add/remove/clear/list] 以 添加/移除/清空/显示 加群审批-自动同意 关键字
                            
                            Tips:
                            1. 关键字不区分大小写
                            2. 只要申请信息中包含关键字就会自动同意
                        """.trimIndent())
                }
                
            }

            "reject" {
                "add" {
                    any { list ->
                        var str = ""
                        list.forEach { str += it }
                        group.getSGroup().autoReject += str
                        sendMsg(description, "关键字添加成功:\n$str")
                    }
                }

                "remove" {
                    any { list ->
                        val first = list.first
                        if(first.isInteger()){
                            val order = first.toInt()
                            val accList = group.getSGroup().autoReject

                            if((order - 1) in accList.indices){
                                sendMsg(description, "关键字 $order: ${accList[order - 1]}\n移除成功！")
                                accList.removeAt(order - 1)
                            }
                            else{
                                sendMsg(description, "移除失败，不存在序号为 $order 的关键字！")
                            }
                        }
                        else sendMsg(description, "序号只能为数字！")
                    }

                    empty {
                        var msg = """
                                请输入关键字的序号以移除该关键字
                                
                                加群审批-自动拒绝 关键字列表:
                            """.trimIndent()
                        group.getSGroup().autoReject.forEachIndexed { i, str ->
                            msg += "\n${i + 1}. $str"
                        }
                        sendMsg(description, msg)
                    }
                }

                "clear" {
                    group.getSGroup().autoReject.clear()
                    sendMsg(description, "加群审批-自动拒绝 关键字已清空~")
                }

                "list" {
                    var msg = "加群审批-自动拒绝 关键字列表:"
                    group.getSGroup().autoReject.forEachIndexed { i, str ->
                        msg += "\n${i + 1}. $str"
                    }
                    sendMsg(description, msg)
                }

                empty {
                    sendMsg(description, """
                            请加上参数 [add/remove/clear/list] 以 添加/移除/清空/显示 加群审批-自动拒绝 关键字
                            
                            Tips:
                            1. 关键字不区分大小写
                            2. 只要申请信息中包含关键字就会自动拒绝
                        """.trimIndent())
                }

            }
            
            
            empty { 
                sendMsg(description, """
                    命令参数:
                    join  -  入群欢迎
                    leave  -  退群提示
                    apply  -  加群审批-自动同意
                    reject  -  加群审批-自动拒绝
                """.trimIndent())
            }
        }
        
    }
    
}

object SCMiraiCode : RawCommand(
    PluginMain,
    "MiraiCode", "code", "Mirai码", "码",
    usage = "Mirai码", description = "Mirai码",
    parentPermission = PERM_EXE_USER
) {
    val userGetMiraiCode = HashMap<User, Long>()
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        
        processSCommand(args) {
            "send" {
                anyContents { code ->
                    sendMsg(code.deserializeMiraiCode())
                }
            }
            
            "get" {
                empty { 
                    val user = user ?: return@empty
                    userGetMiraiCode[user] = System.currentTimeMillis()
                    sendMsg(description, "请在一分钟内发送要取码的消息")
                }
            }
            
            empty { 
                sendMsg(usage, """
                    命令参数:
                    send  -  发送mirai码
                    get  -  获取mirai码
                """.trimIndent())
            }
        }
    }
}

object SCMoeImage : RawCommand(
    PluginMain,
    "MoeImage", "moe", "动漫图片",
    usage = "动漫图片", description = "动漫图片",
    parentPermission = PERM_EXE_3
) {
    private const val apiUrl = "https://api.fantasyzone.cc/tu/?type=url&"
    private const val apiPcUrl = apiUrl + "class=pc"
    
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val contact = subject ?: return
        val user = user ?: return
        
        if(!user.isSunnyAdmin() || args.isEmpty()) {
            GlobalScope.launch(SCoroutine.image) {
                SRequest(apiPcUrl).resultImage(contact)?.let { image ->
                    contact.sendMsg(description, image)
                }
            }
            return
        }
        
        processSCommand(args) {
            anyContents(false) { 
                GlobalScope.launch(SCoroutine.image) {
                    SRequest(apiUrl + it).resultImage(contact)?.let { image ->
                        sendMsg(description, image)
                    }
                }
            }
        }
        
    }
}

object SCGaoKaoCountDown : RawCommand(
    PluginMain,
    "GaoKaoCountDown", "高考倒计时", "gk",
    usage = "高考倒计时", description = "高考倒计时",
    parentPermission = PERM_EXE_MEMBER
) {
    const val MONTH = 6
    const val DATE = 7
    const val TIME = "-06-07 09:00:00"

    val simpleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val group = subject as? Group ?: return
        val member = user as? Member ?: return
        
        processSCommand(args) {
            "on" {
                empty {
                    if(member.isOperator() || member.isSunnyAdmin()){
                        group.getSGroup().isGaoKaoCountDown = true
                        sendMsg(description, "高考倒计时每日提醒 已开启")
                    } else sendMsg(description, At(member) + " 权限不足！")
                }
            }
            
            "off" {
                empty {
                    if(member.isOperator() || member.isSunnyAdmin()){
                        group.getSGroup().isGaoKaoCountDown = false
                        sendMsg(description, "高考倒计时每日提醒 已关闭")
                    } else sendMsg(description, At(member) + " 权限不足！")
                }
            }

            empty {
                sendMsg(description, getCountDownContent() + "\n\nTip: 发送 /gk on 或 /gk off\n  以 开启/关闭 高考倒计时每日提醒")
            }
        }
    }

    fun getCountDownContent(): String {
        val dateNow = STimer.calendar.time
        val (year, month, date, hour, minute, second) = STimer.getTime()
        val timeGaoKao = when(month) {
            in 1..6 -> "$year$TIME"
            in 7..12 -> "${year + 1}$TIME"
            else -> return ""
        }
        val dateGaoKao = simpleFormat.parse(timeGaoKao)

        val str = StringBuilder()
        str.append("""
            当前时间: ${simpleFormat.format(dateNow)}
            高考时间: $timeGaoKao
            
        """.trimIndent())

        when(month) {
            in 1..5 -> str.append("距离高考还有: ${calculate(dateNow, dateGaoKao)}")

            6 -> {
                when(date) {
                    in 1..6 -> str.append("距离高考还有: ${calculate(dateNow, dateGaoKao)}")
                    in 7..10 -> str.append("高考进行中！")
                    in 11..30 -> str.append("高考已结束，请等待成绩公布")
                }
            }

            in 7..12 -> str.append("距离高考还有: ${calculate(dateNow, dateGaoKao)}")
        }

        return str.toString()
    }


    fun calculate(date1: Date, date2: Date): String {
        val between = date2.time - date1.time
        val day = between / (24 * 60 * 60 * 1000)
        val hour = between / (60 * 60 * 1000) - day * 24
        val minute = between / (60 * 1000) - day * 24 * 60 - hour * 60
        val second = between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60

        return "${day}天 ${hour}时 ${minute}分 ${second}秒"
    }
}

object SCReminder : RawCommand(
    PluginMain,
    "Reminder", "rem", "提醒",
    usage = "定时提醒", description = "定时提醒",
    parentPermission = PERM_EXE_MEMBER
) {
    val format = SimpleDateFormat("HH:mm")
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val group = subject as? Group ?: return
        val member = user as? Member ?: return

        if(!member.isOperator() && !member.isSunnyAdmin()){
            sendMsg(description, At(member) + " 您不是管理员，不能使用该功能！")
            return
        }
        
        processSCommand(args) {
            "set" {
                empty { 
                    sendMsg(description, "Tip: /rem set [HH:mm] [提醒事项]")
                }
                
                any { 
                    val first = it.firstOrNull() ?: return@any
                    val str = first.split(":")
                    if(str.size != 2 || str[0].length != 2 || str[1].length != 2) {
                        sendMsg(description, "时间格式错误 请使用 HH:mm 即 小时(24h制):分钟\n例: 09:00")
                        return@any
                    }
                    val date = format.parse(first) ?: kotlin.run {
                        sendMsg(description, "时间格式错误 请使用 HH:mm 即 小时(24h制):分钟\n例: 09:00")
                        return@any
                    }
                    
                    var text = ""
                    for(i in 1 until it.size) {
                        it.getOrNull(i)?.let { string -> text += string.replace(" ", "") }
                    }
                    if(text == "") {
                        sendMsg(description, "请输入提醒事项")
                        return@any
                    }
                    
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    group.getSGroup().reminders += DataReminder(hour, minute, text)
                    
                    sendMsg(description, "成功添加每日 $hour:$minute 的提醒事项:\n$text")
                }
            }
            
            "list" {
                empty {
                    var msg = "所有提醒项:"
                    group.getSGroup().reminders.forEachIndexed { i, rem ->
                        msg += "\n${i + 1}. $rem"
                    }
                    sendMsg(description, msg)
                }
            }

            "remove" {
                any { list ->
                    val first = list.first
                    if(first.isInteger()) {
                        val order = first.toInt()
                        val remList = group.getSGroup().reminders

                        if((order - 1) in remList.indices) {
                            val rem = remList[order - 1]
                            sendMsg(description, "提醒项 $order: $rem\n移除成功！")
                            remList.removeAt(order - 1)
                        } else sendMsg(description, "移除失败，不存在序号为 $order 的提醒项！")
                    } else sendMsg(description, "序号只能为数字！")
                }

                empty {
                    sendMsg(description, """
                                请输入提醒项的序号以移除该提醒项
                                Tip: 输入 /rem list 以查看所有提醒项
                            """.trimIndent())
                }
            }
            
            "edit" {
                text {
                    if(tempText.isInteger()) {
                        val order = tempText.toInt()
                        val remList = group.getSGroup().reminders

                        if((order - 1) in remList.indices) {
                            val rem = remList[order - 1]
                            
                            "once" {
                                empty { 
                                    rem.isOnce = !rem.isOnce
                                    sendMsg(description, "已将序号为 $order 的每日提醒项的 [仅一次] 状态设置为: ${rem.isOnce}")
                                }
                            }
                            
                            "atall" {
                                empty {
                                    rem.isAtAll = !rem.isAtAll
                                    sendMsg(description, "已将序号为 $order 的每日提醒项的 [At全体成员] 状态设置为: ${rem.isAtAll}")
                                }
                            }
                            
                            empty {
                                sendMsg(description, """
                                    命令参数:
                                    once  -  仅一次
                                    atall  -  At全体成员
                                """.trimIndent())
                            }
                            
                        } else sendMsg(description, "编辑失败，不存在序号为 $order 的提醒项！")
                    } else sendMsg(description, "序号只能为数字！")
                }
                
                empty { 
                    sendMsg(description, """
                        请输入提醒项的序号以编辑改提醒项
                        Tip: 输入 /rem edit [序号]
                    """.trimIndent())
                }
            }

            empty {
                sendMsg(description, """
                    命令参数:
                    set  -  设置定时提醒项
                    list  -  显示所有提醒项
                    remove  -  删除定时提醒项
                    edit - 编辑定时提醒项
                """.trimIndent())
            }
        }
    }
}

object SCRcon : RawCommand(
    PluginMain,
    "执行指令", "rcon",
    usage = "Minecraft服务器指令远程执行", description = "Minecraft服务器指令远程执行",
    parentPermission = PERM_EXE_USER
) {
    
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val user = user ?: return
        
        processSCommand(args) {
            "bind" {
                if(this@onCommand !is MemberCommandSender) {
                    any { list ->
                        val symbol = list.first
                        if(symbol.isLetterDigitOrChinese()) {
                            if(list.size >= 3) {
                                val ip = list[1]
                                val strList = ip.split(':')
                                if(strList.size == 2) {
                                    val port = strList[1]
                                    if(port.isInteger()) {

                                        val key = RconData.buildKey(user.id, ip)
                                        val data = SunnyData.rcon[key]
                                        if(data == null) {
                                            val rcon = RconManager.open(strList[0], port.toInt(), list[2])
                                            if(rcon != null) {
                                                SunnyData.rcon[key] = RconData(user.id, ip, list[2])
                                                user.getSPlayer().apply {
                                                    rconKeyMap[symbol] = key
                                                    selectedRconSymbol = symbol
                                                }
                                                sendMsg(description, "绑定成功!\n$symbol -> $key")
                                            } else sendMsg(description, "绑定失败= =\n请确保服务器IP正确且当前服务器在线！")
                                        } else {
                                            val sPlayer = user.getSPlayer()
                                            val map = sPlayer.rconKeyMap
                                            val theKey = map[symbol]
                                            if(theKey != null && theKey == key) {
                                                sendMsg(description, "绑定失败= =\n不能重复绑定")
                                            } else {
                                                map[symbol] = key
                                                sPlayer.selectedRconSymbol = symbol
                                                sendMsg(description, "绑定成功!\n$symbol -> $key")
                                            }
                                        }
                                        
                                    } else sendMsg(description, "[RCON端口] 只能为数字")
                                } else sendMsg(description, "[服务器IP]:[RCON端口] 格式不正确")
                            } else sendMsg(description, "请在 [服务器代号] 后输入 [服务器IP]:[RCON端口] [RCON密码]")
                        } else sendMsg(description, "服务器代号只能含有英文、数字、汉字，不能包含任何符号！")
                    }

                    empty {
                        sendMsg(
                            description, """
                            请在bind后输入: [服务器代号] [服务器IP]:[RCON端口] [RCON密码]
                            例: /rcon bind a aminecraft.cc:25575 123456789
                            
                            Tips:
                            服务器代号只能含有英文、数字、汉字，不能包含任何符号！
                            关于服务器RCON配置请参考wiki: https://wiki.vg/RCON
                        """.trimIndent())
                    }
                } else sendMsg(description, "bind指令只允许在私聊中使用!")
            }
            
            "unbind" {
                anyContents(false) {
                    val rconKeyMap = user.getSPlayer().rconKeyMap
                    if(rconKeyMap.containsKey(it)) {
                        rconKeyMap.remove(it)
                        sendMsg(description, "服务器代号 '$it' 删除成功~")
                    } else sendMsg(description, "服务器代号 '$it' 不存在！")
                }
                
                empty { 
                    sendMsg(description, "/rcon unbind [服务器代号]\n解绑服务器")
                }
            }

            "list" {
                empty {
                    val rconKeyMap = user.getSPlayer().rconKeyMap
                    if(rconKeyMap.isEmpty()) {
                        sendMsg(description, "您未绑定任何RCON")
                        return@empty
                    }

                    val symbolIps = StringBuilder("已绑定的所有RCON:")
                    var index = 1
                    rconKeyMap.forEach { (symbol, key) ->
                        symbolIps.append("\n$index. $symbol -> $key")
                        index++
                    }

                    sendMsg(description, symbolIps.toString())
                }
            }
            
            "run" {
                anyContents { cmd ->
                    val sPlayer = user.getSPlayer()
                    val symbol = sPlayer.selectedRconSymbol
                    if(symbol != "") {
                        sPlayer.rconKeyMap[symbol]?.let { key ->
                            SunnyData.rcon[key]?.let { data ->
                                
                                val executor = data.checkExecutor(user.id)
                                if(executor == RconData.Executor.OWNER || executor == RconData.Executor.OPERATOR) {
                                    RconManager.open(data)?.let { rcon ->
                                        sendMsg(description, rcon.command(cmd))
                                        
                                        if(executor == RconData.Executor.OPERATOR) {
                                            sunnyBot.getUser(data.owner)?.let { owner ->
                                                sunnyScope.launch {
                                                    owner.sendMsg(description, "服务器 $symbol 的管理员 ${user.id} 执行了指令:\n/$cmd")
                                                }
                                            }
                                        }
                                        
                                        return@anyContents
                                    }

                                    sendMsg(description, "RCON '$key'\n连接失败")
                                    return@anyContents
                                } else sendMsg(description, "您无权访问此RCON")
                                
                            }
                            
                            sendMsg(description, "RCON '$key'\n不存在，请重新绑定")
                            return@anyContents
                        }
                    }
                    
                    sendMsg(description, "未选择服务器代号\n请输入 /rcon select [服务器代号]\n进行选择")
                }
                
                empty { 
                    sendMsg(description, "/rcon run [指令]\n向已选择的服务器发送并执行指令")
                }
            }
            
            "select" {
                anyContents(false) { 
                    val sPlayer = user.getSPlayer()
                    if(sPlayer.rconKeyMap.containsKey(it)) {
                        sPlayer.selectedRconSymbol = it
                        sendMsg(description, "服务器代号 '$it' 选择成功")
                    } else sendMsg(description, "未绑定服务器代号 '$it'")
                }
                
                empty { 
                    sendMsg(description, "/rcon select [服务器代号]\n选择服务器代号")
                }
            }
            
            "permit" {
                any { list -> 
                    val idStr = list.first
                    if(idStr.isInteger()) {
                        if(list.size >= 2) {
                            val symbol = list[1]
                            val key = user.getSPlayer().rconKeyMap[symbol]
                            if(key != null) {
                                val data = SunnyData.rcon[key]
                                if(data != null) {
                                    if(data.owner == user.id) {
                                        val id = idStr.toLong()
                                        data.operators += id
                                        SSavePlayer.getSPlayer(id).rconKeyMap[symbol] = key
                                        sendMsg(description, "$id 已获得\n$key 的指令执行权限")
                                    } else sendMsg(description, "您不是此RCON的所有者，不能给他人授权")
                                } else sendMsg(description, "RCON '$key'\n不存在，请重新绑定")
                            } else sendMsg(description, "服务器代号 '$symbol' 不存在")
                        } else sendMsg(description, "请在 [被许可人ID] 后输入 [服务器代号]")
                    } else sendMsg(description, "[被许可人ID](QQ号) 只能为数字")
                }
                
                empty { 
                    sendMsg(description, "/rcon permit [被许可人ID] [服务器代号]\n授予他人指令执行权限")
                }
            }

            empty {
                sendMsg(
                    description, """
                    命令参数:
                    bind  -  绑定服务器
                    unbind  -  解绑服务器
                    list  -  显示所有已绑定的服务器
                    run  -  执行指令
                    select  -  选择服务器代号
                    permit  -  授予他人指令执行权限
                """.trimIndent())
            }
        }
    }
}

object SCRconRun : RawCommand(
    PluginMain,
    "快捷执行指令", "rr",
    usage = "Minecraft服务器指令远程快捷执行", description = "Minecraft服务器指令远程执行",
    parentPermission = PERM_EXE_USER
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val user = user ?: return
        
        processSCommand(args) {
            anyContents { cmd ->
                val sPlayer = user.getSPlayer()
                val symbol = sPlayer.selectedRconSymbol
                if(symbol != "") {
                    sPlayer.rconKeyMap[symbol]?.let { key ->
                        SunnyData.rcon[key]?.let { data ->

                            val executor = data.checkExecutor(user.id)
                            if(executor == RconData.Executor.OWNER || executor == RconData.Executor.OPERATOR) {
                                RconManager.open(data)?.let { rcon ->
                                    sendMsg(description, rcon.command(cmd))

                                    if(executor == RconData.Executor.OPERATOR) {
                                        sunnyBot.getUser(data.owner)?.let { owner ->
                                            sunnyScope.launch {
                                                owner.sendMsg(description, "服务器 $symbol 的管理员 ${user.id} 执行了指令:\n/$cmd")
                                            }
                                        }
                                    }

                                    return@anyContents
                                }

                                sendMsg(description, "RCON '$key'\n连接失败")
                                return@anyContents
                            } else sendMsg(description, "您无权访问此RCON")

                        }

                        sendMsg(description, "RCON '$key'\n不存在，请重新绑定")
                        return@anyContents
                    }
                }

                sendMsg(description, "未选择服务器代号\n请输入 /rcon select [服务器代号]\n进行选择")
            }

            empty {
                sendMsg(description, "/rcon run [指令]\n向已选择的服务器发送并执行指令")
            }
        }
    }
}