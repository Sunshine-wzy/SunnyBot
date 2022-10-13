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
    //指令注册

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
    "Menu", "cd", "菜单", "功能",
    description = "菜单",
    parentPermission = PERM_EXE_USER
) {
    private val menuImage: BufferedImage by lazy {
        val text = StringBuilder()
        PluginMain.registeredCommands.forEach {
            if (it.usage.contains("Debug")) return@forEach

            text.append("◆ ${it.usage.replaceFirst("\n", "")}\n")

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
        } ?: sendMsg(description, "图片加载失败")
    }
}

object SCGameMenu : SimpleCommand(
    PluginMain,
    "GameMenu", "game", "游戏", "游戏菜单",
    description = "游戏菜单",
    parentPermission = PERM_EXE_MEMBER
) {
    val message: String by lazy {
        val str = StringBuilder()
        str.append("===============\n")
        SGameManager.sGroupGameHandlers.forEach {
            str.append("◆ ${it.name}\n")
        }
        str.append(
            """
            ===============
            请输入 '#游戏名称'
            以开始一局游戏
            
            [例] #24点
        """.trimIndent()
        )
        str.toString()
    }


    @Handler
    suspend fun CommandSender.handle() {
        subject?.sendMsg("游戏菜单", message)
    }
}

object SCInfo : SimpleCommand(
    PluginMain,
    "信息", "info",
    description = "查询个人信息",
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
                    PlainText("您的STD余额为: ${sPlayer.std}")
            )
            return
        }
        sendMessage(
            "[Sunshine Technology Dollar]\n" +
                "您的STD余额为: ${sPlayer.std}"
        )
    }
}

object SCAntiRecall : SimpleCommand(
    PluginMain,
    "AntiRecall", "atrc", "防撤回",
    description = "启用/关闭防撤回",
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
            if (msg.contains("开") || msg.contains("t"))
                antiRecall?.setAntiRecallStatus(group.id, true)
            else if (msg.contains("关") || msg.contains("f"))
                antiRecall?.setAntiRecallStatus(group.id, false)
            sendMessage("防撤回状态为: ${antiRecall?.checkAntiRecallStatus(group.id)}")
        } else {
            sendMessage(At(member).plus(PlainText("您不是群主或管理员，没有启用/关闭防撤回功能的权限！")))
        }
    }
}

object SCRepeater : SimpleCommand(
    PluginMain,
    "Repeater", "rep", "复读",
    description = "开启/关闭 复读",
    parentPermission = PERM_EXE_2
) {
    @Handler
    suspend fun MemberCommandSender.handle(isRepeat: String) {
        val rep = isRepeat.lowercase(Locale.getDefault())
        val sGroup = group.getSGroup()

        if (!user.isOperator() && !user.isSunnyAdmin()) {
            sendMessage(At(user).plus(PlainText("您不是群主或管理员，没有启用/关闭复读功能的权限！")))
            group.sendMsg("复读", "群复读状态: ${sGroup.isRepeat}")
            return
        }

        if (rep.contains("t") || rep.contains("开")) {
            sGroup.isRepeat = true
            group.sendMsg("复读", "复读已开启！")
        } else if (rep.contains("f") || rep.contains("关")) {
            sGroup.isRepeat = false
            group.sendMsg("复读", "复读已关闭！")
        } else {
            group.sendMsg("复读", "群复读状态: ${sGroup.isRepeat}")
        }
    }
}

object SCBingPicture : SimpleCommand(
    PluginMain,
    "BingPicture", "bp", "每日一图",
    description = "Bing必应每日一图",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val contact = subject ?: return
        val image = SRequest("https://api.yimian.xyz/img?type=wallpaper").resultImage(contact) ?: kotlin.run {
            contact.sendMsg(description, "图片获取失败...")
            return
        }

        contact.sendMsg(description, image)
    }
}

object SCWeather : SimpleCommand(
    PluginMain,
    "Weather", "天气",
    description = "查询天气",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        val formatter = SimpleDateFormat("yyyy年MM月dd日")
        val date = formatter.format(Date(System.currentTimeMillis()))

        val msg = LightApp(
            """
            {"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气"}
        """.trimIndent()
        )
//        {"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"$city","date":"$date","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}

//        val msg = """
//            mirai:app:{"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1","prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"济南","date":"1月30日 周六","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}
//        """.trimIndent().parseMiraiCode()

        sendMessage(msg)
    }
}

/*
[mirai:source:51993,803246295][[应用]天气]请使用最新版本手机QQ查看
[mirai:app:{"app":"com.tencent.weather","desc":"天气","view":"RichInfoView","ver":"0.0.0.1",
"prompt":"[应用]天气","meta":{"richinfo":{"adcode":"","air":"126","city":"济南","date":"1月30日 周六","max":"13","min":"2","ts":"15158613","type":"201","wind":""}}}]
*/

object SCOpen : SimpleCommand(
    PluginMain,
    "Open", "开关",
    description = "开启/关闭 Sunny",
    parentPermission = PERM_EXE_MEMBER
) {
    @Handler
    suspend fun MemberCommandSender.handle(isOpen: String) {
        val sGroup = group.getSGroup()

        if (!user.isOperator() && !user.isSunnyAdmin()) {
            sendMessage(At(user).plus(PlainText("您不是群主或管理员，没有开启/关闭 本群Bot的权限！")))
            group.sendMsg("Sunny状态", if (sGroup.isOpen) "开启" else "关闭")
            return
        }

        val open = isOpen.lowercase(Locale.getDefault())
        if (open.contains("t") || open.contains("开")) {
            sGroup.isOpen = true
            group.sendMsg("Sunny状态", "Sunny已开启！")
        } else if (open.contains("f") || open.contains("关")) {
            sGroup.isOpen = false
            group.sendMsg("Sunny状态", "Sunny已关闭！")
        } else {
            group.sendMsg("Sunny状态", if (sGroup.isOpen) "开启" else "关闭")
        }
    }
}

object SCSpeed : SimpleCommand(
    PluginMain,
    "Speed", "sp",
    description = "Sunny消息处理速度和统计",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(
            """
Sunny消息统计:
自启动以来共收到了: ${total}条消息
目前的消息处理速度为: ${minute}条/分
                """.trimIndent()
        )
    }
}

object SCGroups : SimpleCommand(
    PluginMain,
    "Groups", "gs",
    description = "查看Sunny总计加了多少个群",
    parentPermission = PERM_EXE_USER
) {
    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(
            """
Sunny目前总共添加了: ${sunnyBot.groups.size}个群聊
        """.trimIndent()
        )
    }
}

object SCListInvite : SimpleCommand(
    PluginMain,
    "ListInvite", "li",
    description = "查看未处理的申请",
    parentPermission = PERM_EXE_1
) {
    @Handler
    suspend fun MemberCommandSender.handle() {
        var lmessage = "目前未处理的群号有(群号:ID):"
        if (group.id == rootgroup.toLong()) {
            invitList.forEach { (t, u) -> lmessage += "\n${u.groupId}:$t" }
        }
        sunnyBot.getGroup(rootgroup.toLong())?.sendMessage(lmessage)
    }
}

object SCAccept : SimpleCommand(
    PluginMain,
    "Accept","aci", "ac",
    description = "查看未处理的申请",
    parentPermission = PERM_EXE_1
) {
    @Handler
    suspend fun MemberCommandSender.handle(id: Int) {
        val event = invitList[id]
        if (event == null) group.sendMessage("不存在的事件ID")
        event?.accept()
        group.sendMessage("成功同意了 ${event?.groupId} 的加群请求")
        event?.invitor?.sendMessage("已同意您对 ${event.groupId} 的加群请求")
        invitList.remove(id)
    }
}