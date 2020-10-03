package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.objects.SPlayerData.sPlayerMap
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.regPlayer
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
}


object SCMenu: SimpleCommand(
    PluginMain,
    "menu", "cd", "菜单", "功能",
    description = "菜单"
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage("\t『 SunnyBot 』\n" +
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
    "serverinfo", "server", "服务器", "服务器状态", "状态",
    description = "服务器状态查询"
) {
    private const val url = "https://mc.iroselle.com/api/data/getServerInfo"
    
    @Handler
    suspend fun CommandSender.handle() {
        val result = SRequest(url).result("happylandmc.cc", 0)
        sendMessage(result)
    }
}