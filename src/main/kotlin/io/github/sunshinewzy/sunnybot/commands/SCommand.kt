package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.commands.SCommandWrapper.Type.*
import io.github.sunshinewzy.sunnybot.getPlainTextContents
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

typealias SCWrapper = SCommandWrapper.() -> Unit

fun CommandSender.processSCommand(args: MessageChain, cmd: SCWrapper) {
    val list = args.getPlainTextContents(LinkedList<String>())
    SCommandWrapper(list).also(cmd)
}

fun Contact.processSCommand(args: MessageChain, cmd: SCWrapper) {
    val list = args.getPlainTextContents(LinkedList<String>())
    SCommandWrapper(list).also(cmd)
}


class SCommandWrapper(val cmdArgs: LinkedList<String>) {
    var shouldContinue = true
    var text = ""
    
    
    private fun wrap(name: String, cmd: SCWrapper, type: Type = NORMAL) {
        var theText = ""
        
        when(type) {
            NORMAL -> {
                if(cmdArgs.isEmpty() || cmdArgs.first != name)
                    return
            }

            EMPTY -> {
                if(cmdArgs.isNotEmpty() && cmdArgs.first != "")
                    return
            }
            
            TEXT -> {
                if(cmdArgs.isEmpty()) return
                theText = cmdArgs.first
            }
            
            ANY -> {}
        }
        
        val list = LinkedList<String>().also { it.addAll(cmdArgs) }
        if(list.isNotEmpty())
            list.removeFirst()
        
        val wrapper = SCommandWrapper(list)
        wrapper.text = theText
        cmd(wrapper)
    }
    
    
    operator fun String.invoke(cmd: SCWrapper) = wrap(this, cmd)
    
    fun text(cmd: SCWrapper) = wrap("", cmd, TEXT)
    
    fun empty(cmd: SCWrapper) = wrap("", cmd, EMPTY)
    
    fun any(cmd: SCWrapper) = wrap("", cmd, ANY)
    
    fun contents(block: (LinkedList<String>) -> Unit) {
        if(cmdArgs.isEmpty()) return
        block(cmdArgs)
    }
    
    fun content(space: Boolean = true, block: (String) -> Unit) {
        if(cmdArgs.isEmpty()) return
        val str = StringBuilder()
        cmdArgs.forEach { str.append("$it ") }
        
        var contents = str.toString()
        if(!space) {
            contents = contents.replace(" ", "")
        }
        
        block(contents)
    }
    
    
    enum class Type {
        NORMAL,
        EMPTY,
        TEXT,
        ANY
    }
}


fun Command.reg(permittee: String = "m*") {
    register()
    setPermit(permission, permittee)
}

fun setPermit(permission: Permission, permittee: String = "m*") {
    AbstractPermitteeId.parseFromString(permittee).permit(permission)
//    BuiltInCommands.PermissionCommand.execute(ConsoleCommandSender, "$permittee $permissionId")
//    ConsoleCommandSender.executeCommand("/permission permit $permittee $permissionId")
}