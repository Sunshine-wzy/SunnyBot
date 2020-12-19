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
    //指令注册
    //默认m*为任意群员 u*为任意用户
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
    usage = "LaTeX渲染" usageWith "/lx LaTeX文本(可以有空格)"
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

        val group = miraiBot?.getGroup(groupId) ?: return
        val image = group.laTeXImage(text)
        group.sendMsg("LaTeX", image)
    }
}

object SCDailySignIn: RawCommand(
    PluginMain,
    "dailySignIn", "qd", "签到", "打卡",
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
        for(i in 0 until dailySignIns.size){
            val signIn = dailySignIns[i]
            msg += "${i + 1}. ${group[signIn.first].nameCard}: " + signIn.second.oldSunSTSymbol(SunSTSymbol.ENTER) + "\n"
        }
        group.sendMsg("每日签到", At(member) + " $msg")
    }
}

object SCServerInfo: RawCommand(
    PluginMain,
    "serverInfo", "server", "zt", "服务器状态", "状态", "服务器",
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

        else if((str == "1" || str.contains("m") || str == "" || args.isEmpty()) && (sGroup.serverIp != "" || sGroup.roselleServerIp != "")) {
            val ip = if(sGroup.serverIp != "") sGroup.serverIp else sGroup.roselleServerIp

            group.sendMsg("服务器状态查询 - Ping", SServerPing.pingServer(ip, str.contains("m")))
        }

        else sendMessage("""
                本群还未绑定服务器
                请输入 "/ip 服务器IP" 以绑定服务器
            """.trimIndent())
    }
}

object SCXmlMessage: RawCommand(
    PluginMain,
    "xmlMessage", "xml",
    usage = "发送一条Xml消息" usageWith "/xml <消息内容>"
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