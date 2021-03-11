package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.listeners.BotListener
import io.github.sunshinewzy.sunnybot.listeners.MessageListener
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.objects.SSaveSunny
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.info

const val NAMESPACE = "Sunny"
var sunnyBot: Bot? = null

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.sunshinewzy.sunnybot",
        version = "1.1.1",
        name = "SunnyBot"
    )
) {
    override fun PluginComponentStorage.onLoad() {
        antiRecall = AntiRecall()
    }
    
    @ExperimentalCommandDescriptors
    @ConsoleExperimentalApi
    override fun onEnable() {
        logger.info { "Hello Sunny!" }
        
        regListeners()
        reloadData()
    }

    override fun onDisable() {
        antiRecall = null
    }
    
    
    @ExperimentalCommandDescriptors
    @ConsoleExperimentalApi
    private fun regListeners() {
        BotListener.listenBot()
        MessageListener.listenMessage()
    }
    
    private fun reloadData() {
        SSavePlayer.reload()
        SSaveGroup.reload()
        SSaveSunny.reload()
    }
}
