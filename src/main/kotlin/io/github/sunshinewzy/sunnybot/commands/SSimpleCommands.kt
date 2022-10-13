package io.github.sunshinewzy.sunnybot.commands

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_1
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_2
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_MEMBER
import io.github.sunshinewzy.sunnybot.PluginMain.PERM_EXE_USER
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.objects.getSPlayer
import io.github.sunshinewzy.sunnybot.utils.SImage
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registeredCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.PlainText
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sunny Simple Commands
 */

fun regSSimpleCommands() {
    //ָ��ע��

    SCMenu.register()
    SCGameMenu.register()
    SCInfo.register()
    SCAntiRecall.register()
    SCRepeater.register()
    SCBingPicture.register()
    SCSpeed.register()
    SCGroups.register()
    SCListInvite.register()
    SCAccept.register()
//    SCOpen.register()

    //Debug
}


object SCMenu : SimpleCommand(
    PluginMain,
    "Menu", "cd", "�˵�", "����",
    description = "�˵�",
    parentPermission = PERM_EXE_USER
) {
    private val menuImage: BufferedImage by lazy {
        val text = StringBuilder()
        PluginMain.registeredCommands.forEach {
            if (it.usage.contains("Debug")) return@forEach

            text.append("�� ${it.usage.replaceFirst("\n", "")}\n")

            it.secondaryNames.forEach { seName ->
                text.append("/$seName  ")
            }
            text.append("\n\n")
        }

        SImage.showTextWithSilverBackground(text.toString())
    }


    @Handler
    suspend fun CommandSender.handle() {
        val subject = subject ?: return

        menuImage.uploadAsImage(subject)?.let {
            sendMsg(description, it)
        } ?: sendMsg(description, "ͼƬ����ʧ��")
    }
}

object SCGameMenu : SimpleCommand(
    PluginMain,
    "GameMenu", "game", "��Ϸ", "��Ϸ�˵�",
    description = "��Ϸ�˵�",
    parentPermission = PERM_EXE_MEMBER
) {
    val message: String by lazy {
        val str = StringBuilder()
        str.append("===============\n")
        SGameManager.sGroupGameHandlers.forEach {
            str.append("�� ${it.name}\n")
        }
        str.append(
            """
            ===============
            ������ '#��Ϸ����'
            �Կ�ʼһ����Ϸ
            
            [��] #24��
        """.trimIndent()
        )
        str.toString()
    }


    @Handler
    suspend fun CommandSender.handle() {
        subject?.sendMsg("��Ϸ�˵�", message)
    }
}

object SCInfo : SimpleCommand(
    PluginMain,
    "��Ϣ", "info",
    description = "��ѯ������Ϣ",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val player = user ?: return
        val sPlayer = player.getSPlayer()

        if (user is Member) {
            val member = user as Member
            sendMessage(
                PlainText("[Sunshine Technology Dollar]\n") + At(member) +
                    PlainText("����STD���Ϊ: ${sPlayer.std}")
            )
            return
        }
        sendMessage(
            "[Sunshine Technology Dollar]\n" +
                "����STD���Ϊ: ${sPlayer.std}"
        )
    }
}

object SCAntiRecall : SimpleCommand(
    PluginMain,
    "AntiRecall", "atrc", "������",
    description = "����/�رշ�����",
    parentPermission = PERM_EXE_2
) {
    @Handler
    suspend fun CommandSender.handle(str: String) {
        if (user == null || user !is Member)
            return
        val member = user as Member
        val group = member.group

        if (member.isOperator() || member.isSunnyAdmin()) {
            val msg = str.lowercase(Locale.getDefault())
            if (msg.contains("��") || msg.contains("t"))
                antiRecall?.setAntiRecallStatus(group.id, true)
            else if (msg.contains("��") || msg.contains("f"))
                antiRecall?.setAntiRecallStatus(group.id, false)
            sendMessage("������״̬Ϊ: ${antiRecall?.checkAntiRecallStatus(group.id)}")
        } else {
            sendMessage(At(member).plus(PlainText("������Ⱥ�������Ա��û������/�رշ����ع��ܵ�Ȩ�ޣ�")))
        }
    }
}

object SCRepeater : SimpleCommand(
    PluginMain,
    "Repeater", "rep", "����",
    description = "����/�ر� ����",
    parentPermission = PERM_EXE_2
) {
    @Handler
    suspend fun MemberCommandSender.handle(isRepeat: String) {
        val rep = isRepeat.lowercase(Locale.getDefault())
        val sGroup = group.getSGroup()

        if (!user.isOperator() && !user.isSunnyAdmin()) {
            sendMessage(At(user).plus(PlainText("������Ⱥ�������Ա��û������/�رո������ܵ�Ȩ�ޣ�")))
            group.sendMsg("����", "Ⱥ����״̬: ${sGroup.isRepeat}")
            return
        }

        if (rep.contains("t") || rep.contains("��")) {
            sGroup.isRepeat = true
            group.sendMsg("����", "�����ѿ�����")
        } else if (rep.contains("f") || rep.contains("��")) {
            sGroup.isRepeat = false
            group.sendMsg("����", "�����ѹرգ�")
        } else {
            group.sendMsg("����", "Ⱥ����״̬: ${sGroup.isRepeat}")
        }
    }
}

object SCBingPicture : SimpleCommand(
    PluginMain,
    "BingPicture", "bp", "ÿ��һͼ",
    description = "Bing��Ӧÿ��һͼ",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val contact = subject ?: return
        val image = SRequest("https://api.yimian.xyz/img?type=wallpaper").resultImage(contact) ?: kotlin.run {
            contact.sendMsg(description, "ͼƬ��ȡʧ��...")
            return
        }

        contact.sendMsg(description, image)
    }
}

object SCWeather : SimpleCommand(
    PluginMain,
    "Weather", "����",
    description = "��ѯ����",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val formatter = SimpleDateFormat("yyyy��MM��dd��")
        val date = formatter.format(Date(System.currentTimeMillis()))

        val msg = LightApp(
            """
            {"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����"}
        """.trimIndent()
        )
//        {"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"$city","date":"$date","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}

//        val msg = """
//            mirai:app:{"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1","prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"����","date":"1��30�� ����","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
//        """.trimIndent().parseMiraiCode()

        sendMessage(msg)
    }
}

/*
[mirai:source:51993,803246295][[Ӧ��]����]��ʹ�����°汾�ֻ�QQ�鿴
[mirai:app:{"app":"com.tencent.weather","desc":"����","view":"RichInfoView","ver":"0.0.0.1",
"prompt":"[Ӧ��]����","meta":{"richinfo":{"adcode":"","air":"126","city":"����","date":"1��30�� ����","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}]
*/

object SCOpen : SimpleCommand(
    PluginMain,
    "Open", "����",
    description = "����/�ر� Sunny",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun MemberCommandSender.handle(isOpen: String) {
        val sGroup = group.getSGroup()

        if (!user.isOperator() && !user.isSunnyAdmin()) {
            sendMessage(At(user).plus(PlainText("������Ⱥ�������Ա��û�п���/�ر� ��ȺBot��Ȩ�ޣ�")))
            group.sendMsg("Sunny״̬", if (sGroup.isOpen) "����" else "�ر�")
            return
        }

        val open = isOpen.lowercase(Locale.getDefault())
        if (open.contains("t") || open.contains("��")) {
            sGroup.isOpen = true
            group.sendMsg("Sunny״̬", "Sunny�ѿ�����")
        } else if (open.contains("f") || open.contains("��")) {
            sGroup.isOpen = false
            group.sendMsg("Sunny״̬", "Sunny�ѹرգ�")
        } else {
            group.sendMsg("Sunny״̬", if (sGroup.isOpen) "����" else "�ر�")
        }
    }
}

object SCSpeed : SimpleCommand(
    PluginMain,
    "Speed", "sp",
    description = "Sunny��Ϣ�����ٶȺ�ͳ��",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(
            """
Sunny��Ϣͳ��:
�������������յ���: ${total}����Ϣ
Ŀǰ����Ϣ�����ٶ�Ϊ: ${minute}��/��
                """.trimIndent()
        )
    }
}

object SCGroups : SimpleCommand(
    PluginMain,
    "Groups", "gs",
    description = "�鿴Sunny�ܼƼ��˶��ٸ�Ⱥ",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(
            """
SunnyĿǰ�ܹ������: ${sunnyBot.groups.size}��Ⱥ��
        """.trimIndent()
        )
    }
}

object SCListInvite : SimpleCommand(
    PluginMain,
    "ListInvite", "li",
    description = "�鿴δ���������",
    parentPermission = PERM_EXE_1
) {
    @Handler
    suspend fun MemberCommandSender.handle() {
        var lmessage = "Ŀǰδ�����Ⱥ����(Ⱥ��:ID):"
        if (group.id == rootgroup.toLong()) {
            invitList.forEach { (t, u) -> lmessage += "\n${u.groupId}:$t" }
        }
        sunnyBot.getGroup(rootgroup.toLong())?.sendMessage(lmessage)
    }
}

object SCAccept : SimpleCommand(
    PluginMain,
    "Accept","aci", "ac",
    description = "�鿴δ���������",
    parentPermission = PERM_EXE_1
) {
    @Handler
    suspend fun MemberCommandSender.handle(id: Int) {
        val event = invitList[id]
        if (event == null) group.sendMessage("�����ڵ��¼�ID")
        event?.accept()
        group.sendMessage("�ɹ�ͬ���� ${event?.groupId} �ļ�Ⱥ����")
        event?.invitor?.sendMessage("��ͬ������ ${event.groupId} �ļ�Ⱥ����")
        invitList.remove(id)
    }
}