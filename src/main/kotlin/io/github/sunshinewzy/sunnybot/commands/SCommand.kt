package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand

suspend fun SimpleCommand.reg(permittee: String) {
    register()
    setPermit(permission.id.toString(), permittee)
}

suspend fun SimpleCommand.reg() {
    reg("m*")
}

suspend fun setPermit(permissionId: String, permittee: String) {
    ConsoleCommandSender.executeCommand("/permission permit $permittee $permissionId")
}

suspend fun setPermit(permissionId: String) {
    setPermit(permissionId, "m*")
}