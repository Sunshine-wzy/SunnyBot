package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.*
import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.functions.Repeater
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup.sGroupMap
import io.github.sunshinewzy.sunnybot.runnable.STimerTask
import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

val sunnyScope = CoroutineScope(SupervisorJob())
val sunnyChannel = sunnyScope.globalEventChannel()
var antiRecall: AntiRecall? = null
//��������Ա
val sunnyAdmins = listOf(1123574549L)

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
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
    SGameManager.gameInit(sunnyBot!!)
    //��ʱ�����ʼ��
    Timer().schedule(STimerTask, Date(), 86400_000L)       //24h = 1440min =  86400s = 86400_000ms
    //����
    Repeater.repeat()
    //���������ļ�
    SunnyBot.downloadVoice()
}

private fun groupInit() {
    sunnyBot?.groups?.forEach {
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
            val id = getGroupID(sender)
            if (id == 0L)
                return@end

            val state = sDataGroupMap[id]?.runningState
            if(state == null || state == RunningState.FREE){
                subject.sendMsg("Game", "��ǰû����Ϸ���ڽ��С�")
                return@end
            }

            subject.sendMsg("Game", "${state.gameName} ��Ϸ����")
            sDataGroupMap[id]?.runningState = RunningState.FREE
        }

        (contains("�����ڰ�")) startAgain@{
            if(sender !is Member)
                return@startAgain
            val member = sender as Member
            val group = getGroup(member) ?: return@startAgain
            val sGroupGameEvent = member.toSGroupGameEvent()

            val lastRunning = sDataGroupMap[getGroupID(sender)]?.lastRunning ?: return@startAgain
            SGameManager.sGroupGameHandlers.forEach { 
                if(it.gameStates.contains(lastRunning)) {
                    it.startGame(sGroupGameEvent)
                    return@startAgain
                }
            }
            group.sendMsg("Game", "��ǰû����Ϸ���ڽ��С�")
        }
        
        atBot {
            val member = sender as Member
            member.group.sendIntroduction()
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

@ExperimentalCommandDescriptors
@ConsoleExperimentalApi
suspend fun setAdministrator() {
    sunnyAdmins.forEach { 
        setPermit("*:*", "u$it")
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
                    SRequest(SCSound.downloadUrl + slug).download(folder.absolutePath, "${sound.title}.amr")
                }
            }
        }
    }
}