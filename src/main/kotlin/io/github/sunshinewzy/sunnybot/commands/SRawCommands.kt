package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.utils.SLaTeX.laTeXImage
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.message.data.MessageChain

/**
 * Sunny Raw Commands
 */

suspend fun regSRawCommands() {
    //指令注册
    //默认m*为任意群员 u*为任意用户
    SCLaTeX.reg("u*")

    //Debug
    SCDebugLaTeX.reg("console")
}


object SCLaTeX: RawCommand(
    PluginMain,
    "LaTeX", "lx",
    description = "LaTeX渲染"
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
    description = "Debug LaTeX"
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