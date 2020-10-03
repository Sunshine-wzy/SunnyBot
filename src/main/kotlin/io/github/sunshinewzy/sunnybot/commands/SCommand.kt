package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand

suspend fun SimpleCommand.reg(permittee: String) {
    register()
    ConsoleCommandSender.executeCommand("/permission permit $permittee ${this.permission.id}")
}

suspend fun SimpleCommand.reg() {
    reg("m*")
}