package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.MessageChain

abstract class SRawCommand(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    owner: CommandOwner,
    /** 主指令名. */
    primaryName: String,
    /** 次要指令名. */
    vararg secondaryNames: String,
    /** 用法说明, 用于发送给用户 */
    usage: String = "<no usages given>",
    /** 指令描述, 用于显示在 [BuiltInCommands.HelpCommand] */
    description: String = "<no descriptions given>",
    /** 指令父权限 */
    parentPermission: Permission = owner.parentPermission,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    prefixOptional: Boolean = false,
) : RawCommand(owner = owner, primaryName = primaryName, secondaryNames = secondaryNames, usage = usage, description = description, parentPermission = parentPermission, prefixOptional = prefixOptional) {
    
    abstract suspend fun executeCommand(sender: CommandSender, args: MessageChain)
    
    
    fun registerSCommand() {
        register()
        SCommandManager.register(this)
    }
    
}