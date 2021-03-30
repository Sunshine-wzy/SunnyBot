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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File

/**
 * Sunny Raw Commands
 */

@ExperimentalCommandDescriptors
@ConsoleExperimentalApi
suspend fun regSRawCommands() {
    //指令注册
    //默认m*为任意群员 u*为任意用户
    SCLaTeX.reg("u*")
    SCDailySignIn.reg("u*")
    SCServerInfo.reg("u*")
    SCXmlMessage.reg("u*")
    SCRedEnvelopes.reg()
    SCRandomImage.reg("u*")
    SCWords.reg("u*")
    SCSound.reg("u*")
    SCGroupManager.reg()
    SCMcbbs.reg()

    //Debug
    SCDebugLaTeX.reg("console")
}


object SCLaTeX: RawCommand(
    PluginMain,
    "LaTeX", "lx",
    usage = "LaTeX渲染" usageWith "/lx LaTeX文本(可以有空格)"
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

        val group = sunnyBot?.getGroup(groupId) ?: return
        val image = group.laTeXImage(text) ?: return
        group.sendMsg("LaTeX", image)
    }
}

object SCDailySignIn: RawCommand(
    PluginMain,
    "DailySignIn", "qd", "签到", "打卡",
    usage = "每日签到" usageWith "/签到 <您的今日赠言>"
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
    usage = "服务器状态查询" usageWith """
        /zt         默认查询方式
        /zt 1       强制使用Ping查询
        /zt 2       强制使用洛神云查询
        /zt m       显示详细mod信息
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

            var serverStatus = "离线"
            if(res.server_status == 1)
                serverStatus = "在线"

            group.sendMsg("服务器状态查询 - 洛神云",
                    "服务器IP: $ip\n" +
                    "服务器状态: $serverStatus\n" +
                    "当前在线玩家数: ${res.server_player_online}\n" +
                    "在线玩家上限: ${res.server_player_max}\n" +
                    "日均在线人数: ${res.server_player_average}\n" +
                    "历史最高同时在线人数: ${res.server_player_history_max}\n" +
                    "昨日平均在线人数: ${res.server_player_yesterday_average}\n" +
                    "昨日最高同时在线人数: ${res.server_player_yesterday_max}\n" +
                    "更新时间: ${res.update_time}\n" +
                    "查询用时: ${result.run_time}s"
            )
        }

        else if((str == "1" || str == "m" || str == "" || args.isEmpty()) && (sGroup.serverIp != "" || sGroup.roselleServerIp != "")) {
            val ip = if(sGroup.serverIp != "") sGroup.serverIp else sGroup.roselleServerIp

            group.sendMsg("服务器状态查询 - Ping", group.pingServer(ip, str.contains("m")))
        }
        
        else if(str != ""){
            if(SServerPing.checkServer(str))
                group.sendMsg("服务器状态查询 - Ping", group.pingServer(str, true))
            else group.sendMsg("服务器状态查询 - Ping", "查询失败= =\n" +
                "请确保服务器IP正确且当前服务器在线！")
        }

        else sendMessage("""
                本群还未绑定服务器
                请输入 "/ip 服务器IP" 以绑定服务器
            """.trimIndent())
    }
}

object SCXmlMessage: RawCommand(
    PluginMain,
    "XmlMessage", "xml",
    usage = "发送一条Xml消息" usageWith "/xml <消息内容>"
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
    usage = "发送一个红包消息" usageWith "/红包 <红包内容>"
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
    usage = "随机图片"
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
            return
        }
        
        contact.sendMsg(description, img)
    }

}

object SCWords : RawCommand(
    PluginMain,
    "Words", "yy", "一言",
    description = "一言",
    usage = "一言"
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
    usage = "语音"
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
    usage = "群管理", description = "群管理"
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
                
                empty {
                    sendMsg(description, "请加上参数 [set/remove] 以 设置/移除 入群欢迎")
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

                empty {
                    sendMsg(description, "请加上参数 [set/remove] 以 设置/移除 退群提示")
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

object SCMcbbs : RawCommand(
    PluginMain,
    "mcbbs",
    usage = "MCBBS", description = "MCBBS"
) {
    private const val urlName1 = "https://www.mcbbs.net/home.php?username="
    private const val urlName2 = "&uid=&gender=0&startage=&endage=&resideprovince=&birthprovince=&birthyear=0&birthmonth=0&birthday=0&searchsubmit=true&op=&mod=spacecp&ac=search&type=all"
    
    val params = listOf("" to "").toCommandParams()
    
    
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        processSCommand(args) {
//            "search" {
//                any { list -> 
//                    val name = list.first
//                    val url = urlName1 + name + urlName2
//                    
//                    sendMsg(description, SRequest(url).result())
//                }
//                
//                empty { 
//                    sendMsg(description, "请输入用户名")
//                }
//            }
            
            empty { 
                sendMsg(description, params)
            }
        }
    }

}