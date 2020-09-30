package io.github.sunshinewzy.sunnybot

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User

val PERMIT_MENU by lazy {
    PermissionService.INSTANCE.register(
        PermissionId(NAMESPACE, "menu"),
        "菜单"
    )
}

fun sunnyInit() {
    SCMenu.register()
}


object SCMenu : SimpleCommand(
    PluginMain,
    "menu", "cd",
    description = "菜单"
) {
    
    
    @Handler
    suspend fun CommandSender.handle() {
        if (this.hasPermission(PERMIT_MENU)) {
            sendMessage("你有 ${PERMIT_MENU.id} 权限.")
        } else {
            sendMessage(
                """
                你没有 ${PERMIT_MENU.id} 权限.
                可以在控制台使用 /permission 管理权限.
            """.trimIndent()
            )
        }
        
        sendMessage("-----=====SunshineBot=====-----\n"
            + "1. 24点")
    }
}


fun getGroup(sender: User): Group? {
    if (sender is Member)
        return sender.group

    return null
}

fun getGroupID(sender: User): Long {
    if (sender is Member)
        return sender.group.id

    return 0
}