package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.listeners.listenBot
import io.github.sunshinewzy.sunnybot.listeners.listenMessage
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

const val NAMESPACE = "Sunny"
var miraiBot: Bot? = null

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.sunshinewzy.sunnybot",
        version = "1.0.0"
    )
) {
    override fun onEnable() {
        logger.info { "Hello Sunny!" }
        
        regListeners()
    }

    override fun onDisable() {
        
    }
    
    
    private fun regListeners() {
        listenBot()
        listenMessage()
    }
}
