package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.commands.regSSimpleCommands
import io.github.sunshinewzy.sunnybot.commands.setPermit
import io.github.sunshinewzy.sunnybot.functions.AntiRecall
import io.github.sunshinewzy.sunnybot.games.SGHour24
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.SGroupData.sGroupMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.subscribeMessages

val sunnyScope = CoroutineScope(SupervisorJob())
var antiRecall: AntiRecall? = null
//��������Ա
val sunnyAdmins = listOf("1123574549")

suspend fun sunnyInit() {
    //ȫ����Ϣ����
    regMsg()
    //ע���ָ��
    regSSimpleCommands()
    //���ó�������Ա (Ȩ��:"*:*")
    setAdministrator()
    //����Ȩ��
    setPermissions()
    //��Ϸ���ܳ�ʼ��
    SGameManager.gameInit(miraiBot!!)
}

private fun regMsg() {
    miraiBot?.subscribeMessages {
        (contains("���Ӳ���")) end@{
            val id = getGroupID(sender)
            if (id == 0L)
                return@end

            val state = sGroupMap[id]?.runningState
            if(state == null || state == ""){
                reply("��ǰû��û�����ڽ��С�")
                return@end
            }
            
            reply("$state ��Ϸ����")
            sGroupMap[id]?.runningState = ""
        }

        (contains("�����ڰ�")) startAgain@{
            val group = getGroup(sender) ?: return@startAgain

            when (sGroupMap[getGroupID(sender)]?.runningState) {
                "24��" -> SGHour24.startHour24(group)

                else -> reply("��ǰû����Ϸ���ڽ��С�")
            }
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

suspend fun setPermissions() {
    setPermit("console:command.help", "u*")
}

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