package io.github.sunshinewzy.test

import io.github.sunshinewzy.sunnybot.PluginMain
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    PluginMain.load()
    PluginMain.enable()

//    val bot = MiraiConsole.addBot(123456L, "password") {
//        fileBasedDeviceInfo()
//    }.alsoLogin()

    MiraiConsole.job.join()
}