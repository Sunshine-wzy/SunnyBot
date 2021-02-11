package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

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
