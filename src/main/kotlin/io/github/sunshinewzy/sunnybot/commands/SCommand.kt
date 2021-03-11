package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.commands.SCommandWrapper.Type.*
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance
import java.util.*

typealias SCWrapper = SCommandWrapper.() -> Unit

fun CommandSender.processSCommand(args: MessageChain, cmd: SCWrapper) {
    val list = LinkedList<String>()
    args.forEach { 
        if(it is PlainText)
            list += it.content
    }
    
    val wrapper = SCommandWrapper(list)
    cmd(wrapper)
}


class SCommandWrapper(val cmdArgs: LinkedList<String>) {
    
    fun process() {
        
    }
    
    private fun wrap(name: String, cmd: SCWrapper, type: Type = NORMAL) {
        when(type) {
            NORMAL -> {
                if(cmdArgs.isEmpty() || cmdArgs.first != name)
                    return
            }

            EMPTY -> {
                if(cmdArgs.isNotEmpty() && cmdArgs.first != "")
                    return
            }
            
        }
        
        val list = LinkedList<String>().also { it.addAll(cmdArgs) }
        if(type != EMPTY)
            list.removeFirst()
        
        val wrapper = SCommandWrapper(list)
        cmd(wrapper)
    }
    
    
    operator fun String.invoke(cmd: SCWrapper) = wrap(this, cmd)
    
    fun empty(cmd: SCWrapper) = wrap("", cmd, EMPTY)
    
    fun any(block: (LinkedList<String>) -> Unit) {
        if(cmdArgs.isEmpty()) return
        block(cmdArgs)
    }
    
    
    enum class Type {
        NORMAL,
        EMPTY
    }
}


@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun Command.reg(permittee: String = "m*") {
    register()
    setPermit(permission.id.toString(), permittee)
}

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun setPermit(permissionId: String, permittee: String) {
    ConsoleCommandSender.executeCommand("/permission permit $permittee $permissionId")
}

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun setPermit(permissionId: String) {
    setPermit(permissionId, "m*")
}
