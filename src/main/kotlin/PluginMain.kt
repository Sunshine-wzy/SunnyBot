package org.example.mirai.plugin

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import org.example.mirai.plugin.io.github.sunshinewzy.sunnybot.sunnyInit

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.sunshinewzy.sunnybot",
        version = "1.0.0"
    )
) {
    override fun onEnable() {
        logger.info { "Hello Sunny!" }
        
        sunnyInit()
    }
}
