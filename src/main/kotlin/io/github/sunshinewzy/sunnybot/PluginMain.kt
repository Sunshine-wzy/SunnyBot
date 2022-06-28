package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.setPermit
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.listeners.BotListener
import io.github.sunshinewzy.sunnybot.listeners.MessageListener
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.objects.SSaveSunny
import io.github.sunshinewzy.sunnybot.timer.STimer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.info

const val NAMESPACE = "Sunny"
lateinit var sunnyBot: Bot

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.sunshinewzy.sunnybot",
        version = "1.2.0",
        name = "SunnyBot"
    )
) {
    val PERM_ROOT by lazy { PermissionService.INSTANCE.register(permissionId("root"), "根权限") }
    val PERM_EXE_1 by lazy { PermissionService.INSTANCE.register(permissionId("execute1"), "一级执行权限", parent = PERM_ROOT) }
    val PERM_EXE_2 by lazy { PermissionService.INSTANCE.register(permissionId("execute2"), "二级执行权限", parent = PERM_EXE_1) }
    val PERM_EXE_3 by lazy { PermissionService.INSTANCE.register(permissionId("execute3"), "三级执行权限", parent = PERM_EXE_2) }
    val PERM_EXE_MEMBER by lazy { PermissionService.INSTANCE.register(permissionId("execute_member"), "群成员执行权限", parent = PERM_EXE_3) }
    val PERM_EXE_USER by lazy { PermissionService.INSTANCE.register(permissionId("execute_user"), "所有用户执行权限", parent = PERM_EXE_MEMBER) }
    
    
    
    override fun PluginComponentStorage.onLoad() {
        antiRecall = AntiRecall()
    }
    
    @ExperimentalCommandDescriptors
    @ConsoleExperimentalApi
    override fun onEnable() {
        logger.info { "Hello Sunny!" }
        
        regListeners()
        setPermissions()
        reloadData()
        
        runTimer()
    }

    override fun onDisable() {
        antiRecall = null
    }
    
    
    private fun regListeners() {
        BotListener.listenBot()
        MessageListener.listenMessage()
    }
    
    private fun setPermissions() {
        PERM_ROOT
        PERM_EXE_1
        PERM_EXE_2
        PERM_EXE_3
        PERM_EXE_MEMBER
        PERM_EXE_USER

        setPermit(PERM_EXE_USER, "u*")
        setPermit(PERM_EXE_MEMBER, "m*")
    }
    
    private fun reloadData() {
        SSavePlayer.reload()
        SSaveGroup.reload()
        SSaveSunny.reload()
    }
    
    private fun runTimer() {
        Thread(STimer).start()
    }
}
