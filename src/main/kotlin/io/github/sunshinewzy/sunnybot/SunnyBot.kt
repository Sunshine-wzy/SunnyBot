package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.*
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.functions.Repeater
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.SBSounds
import io.github.sunshinewzy.sunnybot.objects.SRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Message
import java.io.File

val sunnyScope = CoroutineScope(SupervisorJob())
val sunnyChannel = sunnyScope.globalEventChannel()
var antiRecall: AntiRecall? = null
//超级管理员
val sunnyAdmins = listOf(1123574549L)

suspend fun sunnyInit() {
    //注册简单指令
    regSSimpleCommands()
    //注册复合指令
    regSCompositeCommands()
    //注册原始指令
    regSRawCommands()
    //设置超级管理员 (权限:"*:*")
    setAdministrator()
    //设置权限
    setPermissions()
    //游戏功能初始化
    SGameManager.gameInit(sunnyBot)
    //定时任务初始化
//    Timer().schedule(STimerTask, Date(), 86400_000L)       //24h = 1440min =  86400s = 86400_000ms
    //复读
    Repeater.repeat()
    //下载语音文件
//    SunnyBot.downloadVoice()
}

/**
 * |    被许可人类型    | 字符串表示示例 | 备注                                  |
 * |:----------------:|:-----------:|:-------------------------------------|
 * |      控制台       |   console   |                                      |
 * |      精确群       |   g123456   | 表示群, 而不表示群成员                   |
 * |      精确好友      |   f123456   | 必须通过好友消息                        |
 * |    精确临时会话    | t123456.789  | 群 123456 内的成员 789. 必须通过临时会话  |
 * |     精确群成员     | m123456.789 | 群 123456 内的成员 789. 同时包含临时会话. |
 * |      精确用户      |   u123456   | 同时包含群成员, 好友, 临时会话            |
 * |      任意群       |     g*      |                                      |
 * |  任意群的任意群员   |     m*      |                                      |
 * |  精确群的任意群员   |  m123456.*  | 群 123456 内的任意成员. 同时包含临时会话.  |
 * | 任意群的任意临时会话 |     t*      | 必须通过临时会话                        |
 * | 精确群的任意临时会话 |  t123456.*  | 群 123456 内的任意成员. 必须通过临时会话   |
 * |      任意好友      |     f*      |                                      |
 * |      任意用户      |     u*      | 任何人在任何环境                        |
 * |      任意对象      |      *      | 即任何人, 任何群, 控制台                 |
 */

fun setPermissions() {
    
}

fun setAdministrator() {
    sunnyAdmins.forEach { 
        setPermit(PermissionService.INSTANCE.rootPermission, "u$it")
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

fun Bot.sendGroupMsg(groupId: Long, title: String, text: String) {
    getGroup(groupId)?.apply { 
        sunnyScope.launch {
            sendMsg(title, text)
        }
    }
}

fun Bot.sendGroupMsg(groupId: Long, title: String, message: Message) {
    getGroup(groupId)?.apply {
        sunnyScope.launch {
            sendMsg(title, message)
        }
    }
}

suspend fun Group.sendIntroduction() {
    val text = listOf(
        "输入/cd 就能知道我能干什么了~",
        "输入/cd 展示功能列表~",
        "你知道吗: 输入/cd /menu /菜单 /功能  都可以展示功能列表哦~",
        "输入/cd 开启新世界~"
    )

    sendMessage(text.random())
}


object SunnyBot {
    val illegalFileName = listOf("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
    
    
    fun downloadVoice() {
        sunnyScope.launch(Dispatchers.IO) {
            val folder = File(PluginMain.dataFolder, "Voice")
            if(!folder.exists())
                folder.mkdirs()

            val files = folder.listFiles() ?: emptyArray()
            val names = ArrayList<String>()
            files.forEach {
                names += it.nameWithoutExtension
            }

            val popular = SRequest(SCSound.popularUrl).resultBean<SBSounds>()
            popular.sounds.forEach { sound ->
                if(!names.contains(sound.title)){
                    val slug = sound.slug
                    SRequest(SCSound.downloadUrl + slug).download(folder.absolutePath, "${sound.title.replace(" ", "")}.amr")
                }
            }
        }
    }
}