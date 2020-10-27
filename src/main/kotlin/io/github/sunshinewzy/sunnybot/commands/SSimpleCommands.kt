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
    //指令注册
    //默认m*为任意群员 u*为任意用户
    SCMenu.reg()
    SCInfo.reg("u*")
    SCAntiRecall.reg()
    SCServerInfo.reg("u*")
    SCIpBind.reg()
    SCLaTeX.reg("u*")
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
            val result = SRequest(url).result(ip)
            val args = result.split("<br>")
            var str = args[1]
            for(i in 2..7){
                str += "\n"
                str += args[i]
            }
            
            group.sendMsg("服务器状态查询", str)
        }

        else sendMessage("""
                本群还未绑定服务器
                请输入 "/ip 服务器IP" 以绑定服务器
            """.trimIndent())
        
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
            val groupId = member.group.id
            if(!sGroupMap.containsKey(groupId)) {
                sGroupMap[groupId] = SGroup(groupId)
            }
            val sGroup = sGroupMap[groupId]!!
            
            val roselleResult = SRequest(SCServerInfo.roselleUrl).roselleResult(serverIp, 0)
            if(roselleResult.code == 1){
                sGroup.roselleServerIp = serverIp
                sGroup.serverIp = ""
                sendMessage("$serverIp 绑定成功！")
                return
            }
            
            val result = SRequest(SCServerInfo.url).result(serverIp)
            if(!result.contains("无法连接该服务器")){
                sGroup.serverIp = serverIp
                sGroup.roselleServerIp = ""
                sendMessage("$serverIp 绑定成功！")
                return
            }
        }
        
        sendMessage("绑定失败= =\n" +
            "请确保服务器IP正确！")
    }
}

object SCLaTeX: SimpleCommand(
    PluginMain,
    "LaTeX", "lx",
    description = "LaTeX渲染"
) {
    @Handler
    suspend fun CommandSender.group(text: String) {
        val contact = this.subject ?: return
        val bimg = SLaTeX.generate(text)
        val image = bimg.upload(contact)
        contact.sendMsg("LaTeX", image)
    }
}
