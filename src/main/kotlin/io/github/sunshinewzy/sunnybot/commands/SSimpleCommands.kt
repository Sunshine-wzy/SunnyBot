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
    //指令注册
    //默认m*为任意群员 u*为任意用户
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
    "menu", "cd", "菜单", "功能",
    description = "菜单"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("""
            『 SunnyBot 』
            ===============
            ◆ 24点
            ◆ 井字棋

            ===============
            请输入  #功能名称  以开始
            [例] #24点
        """.trimIndent()
        )
    }
}

object SCInfo: SimpleCommand(
    PluginMain,
    "info", "信息",
    description = "查询个人信息"
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
                PlainText("您的STD余额为: ${sPlayer.std}"))
            return
        }
        sendMessage("[Sunshine Technology Dollar]\n" +
            "您的STD余额为: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "antiRecall", "atrc", "防撤回",
    description = "启用/关闭防撤回"
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator() || sunnyAdmins.contains(member.id.toString())){
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

object SCServerInfo: SimpleCommand(
    PluginMain,
    "serverInfo", "server", "zt", "服务器状态", "状态", "服务器",
    description = "服务器状态查询"
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

            var serverStatus = "离线"
            if(res.server_status == 1)
                serverStatus = "在线"

            sendMessage(
                "\t『 SunnyBot 』\n" +
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

        else if(sGroup.serverIp != "") {
            val ip = sGroup.serverIp
            
            group.sendMsg("服务器状态查询", SServerPing.pingServer(ip))
            
        }

        else sendMessage("""
                本群还未绑定服务器
                请输入 "/ip 服务器IP" 以绑定服务器
            """.trimIndent())
        
    }
}

object SCDebugServerInfo: SimpleCommand(
    PluginMain,
    "DebugServerInfo", "dServer", "dzt",
    description = "Debug 服务器状态查询"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        val contact = miraiBot?.getGroup(423179929L) ?: return
        
        val roselleResult = SRequest(SCServerInfo.roselleUrl).roselleResult(serverIp, 0)
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

        sendMessage(SServerPing.pingServer(serverIp))
    }
}

object SCIpBind: SimpleCommand(
    PluginMain,
    "ipBind", "ip", "服务器绑定", "绑定",
    description = "服务器状态查询IP绑定"
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
                sendMessage("$serverIp 绑定成功！")
                return
            }
            
            if(SServerPing.checkServer(serverIp)){
                sGroup.serverIp = serverIp
                sGroup.roselleServerIp = ""
                sendMessage("$serverIp 绑定成功！")
                return
            }
        }
        
        sendMessage("绑定失败= =\n" +
            "请确保服务器IP正确且当前服务器在线！")
    }
}

object SCJavaDoc: SimpleCommand(
    PluginMain,
    "JavaDoc", "jd",
    description = "查看常用JavaDoc"
) {
    private val javaDocs = """
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
