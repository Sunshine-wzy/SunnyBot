package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.*
import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.functions.Repeater
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup.sGroupMap
import io.github.sunshinewzy.sunnybot.runnable.STimerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.findIsInstance
import java.io.File
import java.util.*

val sunnyScope = CoroutineScope(SupervisorJob())
val sunnyChannel = sunnyScope.globalEventChannel()
var antiRecall: AntiRecall? = null
//超级管理员
val sunnyAdmins = listOf(1123574549L)

suspend fun sunnyInit() {
    //群初始化
    groupInit()
    //全局消息监听
    regMsg()
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
    Timer().schedule(STimerTask, Date(), 86400_000L)       //24h = 1440min =  86400s = 86400_000ms
    //复读
    Repeater.repeat()
    //下载语音文件
    SunnyBot.downloadVoice()
}

private fun groupInit() {
    sunnyBot.groups.forEach {
        val groupId = it.id
        if(!sGroupMap.containsKey(groupId))
            sGroupMap[groupId] = SGroup(groupId)
        if(!sDataGroupMap.containsKey(groupId))
            sDataGroupMap[groupId] = SDataGroup()
    }
}

private fun regMsg() {
    
    sunnyChannel.subscribeMessages {
        
        (contains("老子不会")) end@{
            val group = subject as? Group ?: return@end
            val data = group.getSData()

            val state = data.runningState
            if(state == RunningState.FREE) {
                subject.sendMsg("Game", "当前没有游戏正在进行。")
                return@end
            }
            
            val players = data.players
            if(players.contains(sender.id) || players.checkTimeout()) {
                group.setRunningState(RunningState.FREE)
                subject.sendMsg("Game", "${state.gameName} 游戏结束")
            } else {
                subject.sendMsg("Game", """
                    您不是当前游戏的玩家
                    在 ${players.timeLeft()}毫秒 后才能结束当前游戏
                """.trimIndent())
            }
            
        }

        (contains("再来亿把")) startAgain@{
            val member = sender as? Member ?: return@startAgain
            val group = member.group
            val data = group.getSData()
            
            val state = data.runningState
            val lastState = data.lastRunningState
            if(state == RunningState.FREE) {
                if(lastState != RunningState.FREE) {
                    SGameManager.callGame(member, lastState.gameName, true)
                } else {
                    group.sendMsg("Game", "没有游戏记录")
                }
                
                return@startAgain
            }
            
            val players = data.players
            if(players.contains(sender.id) || players.checkTimeout()) {
                SGameManager.callGame(member, state.gameName, true)
            } else {
                subject.sendMsg("Game", """
                    您不是当前游戏的玩家
                    在 ${players.timeLeft()}毫秒 后才能结束当前游戏
                """.trimIndent())
            }
            
        }
        
        (contains("sunny", ignoreCase = true) and (contains("闭嘴") or contains("睡觉") or contains("关"))) sleep@{
            if(sender !is Member)
                return@sleep
            val group = getGroup(sender) ?: return@sleep
            
            group.getSGroup().isOpen = false
            group.sendMessage("Bye~ master")
        }
        
        (contains("sunny", ignoreCase = true) and (contains("醒醒") or contains("启") or contains("开"))) start@{
            if(sender !is Member)
                return@start
            val group = getGroup(sender) ?: return@start
            
            group.getSGroup().isOpen = true
            group.sendMessage("Hi! master")
        }
        
        atBot {
            val member = sender as? Member ?: return@atBot
            val text = message.findIsInstance<PlainText>() ?: kotlin.run { 
                member.group.sendIntroduction()
                return@atBot
            }
            val msg = text.content.replace("\n", "").replace(" ", "")
            if(msg.isEmpty()) {
                member.group.sendIntroduction()
                return@atBot
            }
            
            val ownThink = SRequest("https://api.ownthink.com/bot?spoken=$msg")
                .result<SBOwnThink>()
            if(ownThink.message == "success") {
                member.group.sendMessage(QuoteReply(message) + ownThink.data.info.text)
            } else member.group.sendMessage("思 考 不 能")
            
        }
        
    }
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
    fun downloadVoice() {
        GlobalScope.launch(SCoroutine.download) {
            val folder = File(PluginMain.dataFolder, "Voice")
            if(!folder.exists())
                folder.mkdirs()

            val files = folder.listFiles() ?: emptyArray()
            val names = ArrayList<String>()
            files.forEach {
                names += it.nameWithoutExtension
            }

            val popular = SRequest(SCSound.popularUrl).result<SBSounds>()
            popular.sounds.forEach { sound ->
                if(!names.contains(sound.title)){
                    val slug = sound.slug
                    SRequest(SCSound.downloadUrl + slug).download(folder.absolutePath, "${sound.title.replace(" ", "")}.amr")
                }
            }
        }
    }
}