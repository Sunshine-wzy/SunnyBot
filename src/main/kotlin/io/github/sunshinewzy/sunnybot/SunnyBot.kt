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
//��������Ա
val sunnyAdmins = listOf(1123574549L)

suspend fun sunnyInit() {
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
//    Timer().schedule(STimerTask, Date(), 86400_000L)       //24h = 1440min =  86400s = 86400_000ms
    //����
    Repeater.repeat()
    //���������ļ�
//    SunnyBot.downloadVoice()
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