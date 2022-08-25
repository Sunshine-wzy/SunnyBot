package io.github.sunshinewzy.sunnybot.commands

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.message.data.MessageChain

abstract class SRawCommand(
    /**
     * ָ��ӵ����.
     * @see CommandOwner
     */
    owner: CommandOwner,
    /** ��ָ����. */
    primaryName: String,
    /** ��Ҫָ����. */
    vararg secondaryNames: String,
    /** �÷�˵��, ���ڷ��͸��û� */
    usage: String = "<no usages given>",
    /** ָ������, ������ʾ�� [BuiltInCommands.HelpCommand] */
    description: String = "<no descriptions given>",
    /** ָ�Ȩ�� */
    parentPermission: Permission = owner.parentPermission,
    /** Ϊ `true` ʱ��ʾ [ָ��ǰ׺][CommandManager.commandPrefix] ��ѡ */
    prefixOptional: Boolean = false,
) : RawCommand(owner = owner, primaryName = primaryName, secondaryNames = secondaryNames, usage = usage, description = description, parentPermission = parentPermission, prefixOptional = prefixOptional) {
    
    abstract suspend fun executeCommand(sender: CommandSender, args: MessageChain)
    
    
    fun registerSCommand() {
        register()
        SCommandManager.register(this)
    }
    
}