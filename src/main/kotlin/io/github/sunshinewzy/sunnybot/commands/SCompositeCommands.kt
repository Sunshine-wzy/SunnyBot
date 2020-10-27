package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.utils.SLaTeX
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.message.upload

suspend fun regSCompositeCommands() {
    //指令注册
    //默认m*为任意群员 u*为任意用户
    
    //Debug
    SCDebugLaTeX.reg("console")
}


object SCDebugLaTeX: CompositeCommand(
    PluginMain,
    "debugLaTeX", "dlx",
    description = "Debug LaTeX"
) {
    private const val groupIdSunST = 423179929L

    @SubCommand
    suspend fun ConsoleCommandSender.s(text: String) {
        g(groupIdSunST, text)
    }

    @SubCommand
    suspend fun ConsoleCommandSender.g(groupId: Long, text: String) {
        val group = miraiBot?.getGroup(groupId) ?: return
        val bimg = SLaTeX.generate(text)
        val image = bimg.upload(group)
        group.sendMsg("LaTeX", image)
    }
}