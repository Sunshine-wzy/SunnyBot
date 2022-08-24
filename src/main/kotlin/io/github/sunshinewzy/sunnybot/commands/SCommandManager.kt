package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.toPlainText

object SCommandManager {
    
    private val commandableMap: MutableMap<String, SCommandable> = hashMapOf()
    
    
    fun register(commandable: SCommandable) {
        commandableMap[commandable.sCommandName] = commandable
    }
    
    suspend fun executeCommand(sender: CommandSender, args: MessageChain, text: String) {
        val texts = text.split(' ')
        val firstText = texts.firstOrNull() ?: return
        if(firstText.isEmpty()) return
        
        val cmd = firstText.lowercase()
        if(cmd.isEmpty()) return
        
        val commandable = commandableMap[cmd] ?: return
        val newArgs = buildMessageChain { 
            +args
            removeAll { it is PlainText }
            texts.forEachIndexed { index, string -> 
                if(index == 0) return@forEachIndexed
                +string.toPlainText()
            }
        }
        
        commandable.executeCommand(sender, newArgs)
    }
    
    
    fun SCommandable.registerSCommand() {
        register(this)
    }
    
}