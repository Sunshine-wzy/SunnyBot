package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText

object SCommandManager {
    
    private val commandMap: MutableMap<String, SRawCommand> = hashMapOf()
    
    
    fun register(sRawCommand: SRawCommand) {
        commandMap[sRawCommand.primaryName] = sRawCommand
        sRawCommand.secondaryNames.forEach { 
            commandMap[it] = sRawCommand
        }
    }
    
    suspend fun executeCommand(sender: CommandSender, args: MessageChain, text: String) {
        val texts = text.split(' ')
        val firstText = texts.firstOrNull() ?: return
        if(firstText.isEmpty()) return
        
        val cmd = firstText.lowercase()
        if(cmd.isEmpty()) return
        
        val command = commandMap[cmd] ?: return
        if(!command.permission.testPermission(sender)) {
            sender.sendMessage(args.quote() + "È¨ÏÞ²»×ã")
            return
        }
        
        val newArgs = buildMessageChain { 
            +args
            removeAll { it is PlainText }
            texts.forEachIndexed { index, string -> 
                if(index == 0) return@forEachIndexed
                +string.toPlainText()
            }
        }
        
        command.executeCommand(sender, newArgs)
    }
    
}