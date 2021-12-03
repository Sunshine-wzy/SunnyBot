package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_MEMBER
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_USER
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import io.github.sunshinewzy.sunnybot.utils.SServerPing.pingServer
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registeredCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sunny Simple Commands
 */

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun regSSimpleCommands() {
    //指令注册

    SCMenu.register()
    SCGameMenu.register()
    SCInfo.register()
    SCAntiRecall.register()
    SCJavaDoc.register()
    SCRepeater.register()
    SCBingPicture.register()
    SCOpen.register()
    
    //默认m*为任意群员 u*为任意用户
//    SCMenu.reg("u*")
//    SCGameMenu.reg()
//    SCInfo.reg("u*")
//    SCAntiRecall.reg()
//    SCJavaDoc.reg("u*")
//    SCRepeater.reg()
//    SCBingPicture.reg()
//    SCOpen.reg()
    
    //Debug
    SCDebugServerInfo.reg("console")
    SCDebugIntroduction.reg("console")
}


object SCMenu: SimpleCommand(
    PluginMain,
    "Menu", "cd", "菜单", "功能",
    description = "菜单|功能列表",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        var text = "===============\n"
        PluginMain.registeredCommands.forEach { 
            if(it.usage.contains("Debug")) return@forEach
            
            text += "◆ ${it.usage.replaceFirst("\n", "")}\n"
            
            it.secondaryNames.forEach { seName ->
                text += "/$seName  "
            }
            text += "\n\n"
        }
        text += "===============\n"
        
        subject?.sendMsg("菜单 | 功能列表", text)
    }
}

object SCGameMenu: SimpleCommand(
    PluginMain,
    "GameMenu", "game", "游戏", "游戏菜单",
    description = "游戏菜单",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun CommandSender.handle() {
        subject?.sendMsg("游戏菜单", """
            ===============
            ◆ 24点
            ◆ 井字棋
            ◆ 围棋
            ===============
            请输入 '#游戏名称'
            以开始一局游戏
            
            [例] #24点
        """.trimIndent()
        )
    }
}

object SCInfo: SimpleCommand(
    PluginMain,
    "信息", "info",
    description = "查询个人信息",
    parentPermission = PERM_EXE_USER
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
                PlainText("您的STD余额为: ${sPlayer.std}"))
            return
        }
        sendMessage("[Sunshine Technology Dollar]\n" +
            "您的STD余额为: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "AntiRecall", "atrc", "防撤回",
    description = "启用/关闭防撤回",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator() || member.isSunnyAdmin()){
            val msg = str.toLowerCase()
            if(msg.contains("开") || msg.contains("t"))
                antiRecall?.setAntiRecallStatus(group.id, true)
            else if(msg.contains("关") || msg.contains("f"))
                antiRecall?.setAntiRecallStatus(group.id, false)
            sendMessage("防撤回状态为: ${antiRecall?.checkAntiRecallStatus(group.id)}")
        }
        else{
            sendMessage(At(member).plus(PlainText("您不是群主或管理员，没有启用/关闭防撤回功能的权限！")))
        }
    }
}

object SCDebugServerInfo: SimpleCommand(
    PluginMain,
    "DebugServerInfo", "dServer", "dzt",
    description = "Debug 服务器状态查询"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        val contact = sunnyBot.getGroup(423179929L) ?: return
        
        val roselleResult = SRequest(SCServerInfo.roselleUrl).resultRoselle(serverIp, 0)
        if(roselleResult.code == 1){
            val res = roselleResult.res
            var serverStatus = "离线"
            if(res.server_status == 1)
                serverStatus = "在线"

            sendMessage(
                "\t『 SunnyBot 』\n" +
                    "服务器IP: $serverIp\n" +
                    "服务器状态: $serverStatus\n" +
                    "当前在线玩家数: ${res.server_player_online}\n" +
                    "在线玩家上限: ${res.server_player_max}\n" +
                    "日均在线人数: ${res.server_player_average}\n" +
                    "历史最高同时在线人数: ${res.server_player_history_max}\n" +
                    "昨日平均在线人数: ${res.server_player_yesterday_average}\n" +
                    "昨日最高同时在线人数: ${res.server_player_yesterday_max}\n" +
                    "更新时间: ${res.update_time}\n" +
                    "查询用时: ${roselleResult.run_time}s"
            )
            return
        }

        sendMessage(contact.pingServer(serverIp))
    }
}

object SCJavaDoc: SimpleCommand(
    PluginMain,
    "JavaDoc", "jd",
    description = "查看常用JavaDoc",
    parentPermission = PERM_EXE_USER
) {
    private val javaDocs = """
        OI Wiki: https://oi-wiki.org/
        Java8: https://docs.oracle.com/javase/8/docs/api/overview-summary.html 
        
        Bukkit教程:
        基础 https://alpha.tdiant.net/
        进阶 https://bdn.tdiant.net/
        
        BukkitAPI - Javadoc: 
        1.7.10版(已过时):https://jd.bukkit.org/ 
        Chinese_Bukkit: 
        1.12.2版:http://docs.zoyn.top/bukkitapi/1.12.2/ 
        1.13+版:https://bukkit.windit.net/javadoc/ 
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
    "Repeater", "rep", "复读",
    description = "开启/关闭 复读",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun MemberCommandSender.handle(isRepeat: String) {
        val rep = isRepeat.toLowerCase()
        val sGroup = group.getSGroup()
        
        if(!user.isOperator() && !user.isSunnyAdmin()){
            sendMessage(At(user).plus(PlainText("您不是群主或管理员，没有启用/关闭复读功能的权限！")))
            group.sendMsg("复读", "群复读状态: ${sGroup.isRepeat}")
            return
        }
        
        if(rep.contains("t") || rep.contains("开")){
            sGroup.isRepeat = true
            group.sendMsg("复读", "复读已开启！")
        }
        else if(rep.contains("f") || rep.contains("关")){
            sGroup.isRepeat = false
            group.sendMsg("复读", "复读已关闭！")
        }
        else{
            group.sendMsg("复读", "群复读状态: ${sGroup.isRepeat}")
        }
    }
}

object SCBingPicture : SimpleCommand(
    PluginMain,
    "BingPicture", "bp", "每日一图",
    description = "Bing必应每日一图",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val contact = subject ?: return
        val image = SRequest("https://api.yimian.xyz/img?type=wallpaper").resultImage(contact) ?: kotlin.run { 
            contact.sendMsg(description, "图片获取失败...")
            return
        }
        
        contact.sendMsg(description, image)
    }
}

object SCDebugIntroduction : SimpleCommand(
    PluginMain,
    "DebugIntroduction", "di", "自我介绍",
    description = "Debug 发送自我介绍"
) {
    @Handler
    suspend fun CommandSender.handle(groupId: Long) {
        val group = sunnyBot.getGroup(groupId) ?: kotlin.run { 
            PluginMain.logger.warning("群$groupId 获取失败")
            return
        }
        
        group.sendIntroduction()
    }
}

object SCWeather : SimpleCommand(
    PluginMain,
    "Weather", "天气",
    description = "查询天气",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val formatter = SimpleDateFormat("yyyy年MM月dd日")
        val date = formatter.format(Date(System.currentTimeMillis()))
        
        val msg = LightApp("""
            {"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气"}
        """.trimIndent())
//        {"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"$city","date":"$date","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
        
//        val msg = """
//            mirai:app:{"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"济南","date":"1月30日 周六","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
//        """.trimIndent().parseMiraiCode()
        
        sendMessage(msg)
    }
}

/*
[mirai:source:51993,803246295][[应用]天气]请使用最新版本手机QQ查看
[mirai:app:{"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1",
"prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"济南","date":"1月30日 周六","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}]
*/

object SCOpen : SimpleCommand(
    PluginMain,
    "Open", "开关",
    description = "开启/关闭 Sunny",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun MemberCommandSender.handle(isOpen: String) {
        val sGroup = group.getSGroup()

        if(!user.isOperator() && !user.isSunnyAdmin()){
            sendMessage(At(user).plus(PlainText("您不是群主或管理员，没有开启/关闭 本群Bot的权限！")))
            group.sendMsg("Sunny状态", if(sGroup.isOpen) "开启" else "关闭")
            return
        }

        val open = isOpen.toLowerCase()
        if(open.contains("t") || open.contains("开")){
            sGroup.isOpen = true
            group.sendMsg("Sunny状态", "Sunny已开启！")
        }
        else if(open.contains("f") || open.contains("关")){
            sGroup.isOpen = false
            group.sendMsg("Sunny状态", "Sunny已关闭！")
        }
        else{
            group.sendMsg("Sunny状态", if(sGroup.isOpen) "开启" else "关闭")
        }
    }
}