package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.listeners.listenBot
import io.github.sunshinewzy.sunnybot.listeners.listenMessage
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

const val NAMESPACE = "Sunny"
var miraiBot: Bot? = null

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.sunshinewzy.sunnybot",
        version = "1.0.6"
    )
) {
    override fun PluginComponentStorage.onLoad() {
        antiRecall = AntiRecall()
    }
    
    override fun onEnable() {
        logger.info { "Hello Sunny!" }
        
        regListeners()
        reloadData()
    }

    override fun onDisable() {
        antiRecall = null
    }
    
    
    private fun regListeners() {
        listenBot()
        listenMessage()
    }
    
    private fun reloadData() {
        SSavePlayer.reload()
        SSaveGroup.reload()
    }
}
