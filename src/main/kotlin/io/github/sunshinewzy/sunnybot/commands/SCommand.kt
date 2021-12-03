package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.commands.SCommandWrapper.Type.*
import io.github.sunshinewzy.sunnybot.getPlainText
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance
import java.util.*

typealias SCWrapper = SCommandWrapper.() -> Unit

fun CommandSender.processSCommand(args: MessageChain, cmd: SCWrapper) {
    val list = args.getPlainText(LinkedList<String>())
    val wrapper = SCommandWrapper(list)
    cmd(wrapper)
}


class SCommandWrapper(val cmdArgs: LinkedList<String>) {
    var shouldContinue = true
    
    
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
    
    fun anyContents(space: Boolean = true, block: (String) -> Unit) {
        if(cmdArgs.isEmpty()) return
        var contents = ""
        cmdArgs.forEach { contents += "$it " }
        
        if(!space) {
            contents = contents.replace(" ", "")
        }
        
        block(contents)
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
    BuiltInCommands.PermissionCommand.execute(ConsoleCommandSender, "$permittee $permissionId")
//    ConsoleCommandSender.executeCommand("/permission permit $permittee $permissionId")
}

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
suspend fun setPermit(permissionId: String) {
    setPermit(permissionId, "m*")
}
