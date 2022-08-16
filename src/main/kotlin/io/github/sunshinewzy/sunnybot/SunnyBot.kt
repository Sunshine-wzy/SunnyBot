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
//��������Ա
val sunnyAdmins = listOf(1123574549L)

suspend fun sunnyInit() {
    //Ⱥ��ʼ��
    groupInit()
    //ȫ����Ϣ����
    regMsg()
    //ע���ָ��
    regSSimpleCommands()
    //ע�Ḵ��ָ��
    regSCompositeCommands()
    //ע��ԭʼָ��
    regSRawCommands()
    //���ó�������Ա (Ȩ��:"*:*")
    setAdministrator()
    //����Ȩ��
    setPermissions()
    //��Ϸ���ܳ�ʼ��
    SGameManager.gameInit(sunnyBot)
    //��ʱ�����ʼ��
    Timer().schedule(STimerTask, Date(), 86400_000L)       //24h = 1440min =  86400s = 86400_000ms
    //����
    Repeater.repeat()
    //���������ļ�
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
        
        (contains("���Ӳ���")) end@{
            val group = subject as? Group ?: return@end
            val data = group.getSData()

            val state = data.runningState
            if(state == RunningState.FREE) {
                subject.sendMsg("Game", "��ǰû����Ϸ���ڽ��С�")
                return@end
            }
            
            val players = data.players
            if(players.contains(sender.id) || players.checkTimeout()) {
                group.setRunningState(RunningState.FREE)
                subject.sendMsg("Game", "${state.gameName} ��Ϸ����")
            } else {
                subject.sendMsg("Game", """
                    �����ǵ�ǰ��Ϸ�����
                    �� ${players.timeLeft()}���� ����ܽ�����ǰ��Ϸ
                """.trimIndent())
            }
            
        }

        (contains("�����ڰ�")) startAgain@{
            val member = sender as? Member ?: return@startAgain
            val group = member.group
            val data = group.getSData()
            
            val state = data.runningState
            val lastState = data.lastRunningState
            if(state == RunningState.FREE) {
                if(lastState != RunningState.FREE) {
                    SGameManager.callGame(member, lastState.gameName, true)
                } else {
                    group.sendMsg("Game", "û����Ϸ��¼")
                }
                
                return@startAgain
            }
            
            val players = data.players
            if(players.contains(sender.id) || players.checkTimeout()) {
                SGameManager.callGame(member, state.gameName, true)
            } else {
                subject.sendMsg("Game", """
                    �����ǵ�ǰ��Ϸ�����
                    �� ${players.timeLeft()}���� ����ܽ�����ǰ��Ϸ
                """.trimIndent())
            }
            
        }
        
        (contains("sunny", ignoreCase = true) and (contains("����") or contains("˯��") or contains("��"))) sleep@{
            if(sender !is Member)
                return@sleep
            val group = getGroup(sender) ?: return@sleep
            
            group.getSGroup().isOpen = false
            group.sendMessage("Bye~ master")
        }
        
        (contains("sunny", ignoreCase = true) and (contains("����") or contains("��") or contains("��"))) start@{
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
            } else member.group.sendMessage("˼ �� �� ��")
            
        }
        
    }
}

/**
 * |    �����������    | �ַ�����ʾʾ�� | ��ע                                  |
 * |:----------------:|:-----------:|:-------------------------------------|
 * |      ����̨       |   console   |                                      |
 * |      ��ȷȺ       |   g123456   | ��ʾȺ, ������ʾȺ��Ա                   |
 * |      ��ȷ����      |   f123456   | ����ͨ��������Ϣ                        |
 * |    ��ȷ��ʱ�Ự    | t123456.789  | Ⱥ 123456 �ڵĳ�Ա 789. ����ͨ����ʱ�Ự  |
 * |     ��ȷȺ��Ա     | m123456.789 | Ⱥ 123456 �ڵĳ�Ա 789. ͬʱ������ʱ�Ự. |
 * |      ��ȷ�û�      |   u123456   | ͬʱ����Ⱥ��Ա, ����, ��ʱ�Ự            |
 * |      ����Ⱥ       |     g*      |                                      |
 * |  ����Ⱥ������ȺԱ   |     m*      |                                      |
 * |  ��ȷȺ������ȺԱ   |  m123456.*  | Ⱥ 123456 �ڵ������Ա. ͬʱ������ʱ�Ự.  |
 * | ����Ⱥ��������ʱ�Ự |     t*      | ����ͨ����ʱ�Ự                        |
 * | ��ȷȺ��������ʱ�Ự |  t123456.*  | Ⱥ 123456 �ڵ������Ա. ����ͨ����ʱ�Ự   |
 * |      �������      |     f*      |                                      |
 * |      �����û�      |     u*      | �κ������κλ���                        |
 * |      �������      |      *      | ���κ���, �κ�Ⱥ, ����̨                 |
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
        "����/cd ����֪�����ܸ�ʲô��~",
        "����/cd չʾ�����б�~",
        "��֪����: ����/cd /menu /�˵� /����  ������չʾ�����б�Ŷ~",
        "����/cd ����������~"
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