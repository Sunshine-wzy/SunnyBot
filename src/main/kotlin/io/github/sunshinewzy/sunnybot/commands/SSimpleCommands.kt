package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

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
}


object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd", "菜单", "功能",
    description = "菜单"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t『 SunnyBot 』\n" +
            "===============\n" +
            "◆ 24点" +
            "\n\n===============\n" +
            "请输入  #功能名称  以开始\n" +
            "[例] #24点")
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
            sendMessage(At(member.group[id]).plus(PlainText("您的STD为: ${sPlayer.std}")))
            return
        }
        sendMessage("您的STD为: ${sPlayer.std}")
    }
}

object SCAntiRecall: SimpleCommand(
    PluginMain,
    "antirecall", "atrc", "防撤回",
    description = "启用/关闭防撤回"
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if(user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group
        
        if(member.isOperator()){
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
    "serverinfo", "server", "zt", "服务器状态", "状态", "服务器",
    description = "服务器状态查询"
) {
    const val url = "https://mc.iroselle.com/api/data/getServerInfo"
    const val happylandIp = "happylandmc.cc"
    
    @Handler
    suspend fun CommandSender.handle() {
        var ip = happylandIp
        if(user == null)
            return
        if(user is Member){
            val member = user as Member
            val groupId = member.group.id
            if(sGroupMap.containsKey(groupId))
                ip = sGroupMap[groupId]!!.serverIp
        }
            
        val result = SRequest(url).result(ip, 0)
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
            "查询用时: ${result.run_time}s")
    }
}

object SCIpBind: SimpleCommand(
    PluginMain,
    "ipbind", "ip", "服务器绑定", "绑定",
    description = "服务器状态查询IP绑定"
) {
    @Handler
    suspend fun CommandSender.handle(serverIp: String) {
        if(user !=null && user is Member){
            val member = user as Member
            val groupId = member.group.id
            if(sGroupMap.containsKey(groupId)) {
                val result = SRequest(SCServerInfo.url).result(serverIp, 0)
                if(result.code == 1){
                    sGroupMap[groupId]!!.serverIp = serverIp
                    sendMessage("$serverIp 绑定成功！")
                    return
                }
            }
            else sGroupMap[groupId] = SGroup(groupId)
        }
        
        sendMessage("绑定失败= =\n" +
            "请确保服务器IP正确！")
    }
}