package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.SDataGroup
import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SGroupData
import io.github.sunshinewzy.sunnybot.objects.sDataGroup
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import java.lang.StringBuilder

//region 发送含标题文本
suspend fun Contact.sendMsg(title: Message, text: Message) {
    sendMessage(PlainText("\t『") +
        title + PlainText("』\n") +
        text
    )
}

suspend fun Contact.sendMsg(title: String, text: String) {
    sendMsg(PlainText(title), PlainText(text))
}

suspend fun Contact.sendMsg(title: String, text: Message) {
    sendMsg(PlainText(title), text)
}

suspend fun Contact.sendMsg(title: Message, text: String) {
    sendMsg(title, PlainText(text))
}
//endregion

//region 构造SGroupGameEvent事件
fun Member.toSGroupGameEvent(message: MessageChain): SGroupGameEvent {
    val group = this.group
    val groupId = group.id
    if(!SGroupData.sGroupMap.containsKey(groupId))
        SGroupData.sGroupMap[groupId] = SGroup(groupId)
    val sGroup = SGroupData.sGroupMap[groupId]!!
    if(!sDataGroup.containsKey(groupId))
        sDataGroup[groupId] = SDataGroup()
    val sDataGroup = sDataGroup[groupId]!!
    val msg = message[PlainText.Key]?.contentToString() ?: ""
    
    return SGroupGameEvent(this, group, groupId, sGroup, sDataGroup, msg)
}

fun Member.toSGroupGameEvent(): SGroupGameEvent {
    return toSGroupGameEvent(PlainText("") + PlainText(""))
}
//endregion

//region 十进制状态压缩
/**
 * 获取指定位置的数字
 */
fun Int.getDigit(digit: Int): Int {
    return toString()[digit].toInt()
}

/**
 * 修改指定位置的数字
 * @param digit 位
 * @param num 修改后的数字
 */
fun Int.setDigit(digit: Int, num: Int): Int {
    val str = StringBuilder(toString())
    str[digit] = num.toChar()
    return str.toString().toInt()
}
//endregion