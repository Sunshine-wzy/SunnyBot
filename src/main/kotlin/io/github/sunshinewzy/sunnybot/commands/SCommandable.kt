package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.message.data.MessageChain

interface SCommandable {
    
    val sCommandName: String
    
    
    suspend fun executeCommand(sender: CommandSender, args: MessageChain)
    
}