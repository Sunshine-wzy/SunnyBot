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
import kotlin.math.pow

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
    val str = toString()
    if(str.length < digit){
        return this
    }
    
    return str[digit - 1].toInt()
}

/**
 * 修改指定位置的数字
 * @param digit 位
 * @param num 修改后的数字
 */
fun Int.setDigit(digit: Int, num: Int): Int {
    var ans = zeroEnd(digit) dshr digit - 1
    ans += num
    ans = ans dshl digit - 1
    ans += getEnd(digit - 1)
    return ans
}

/**
 * 十进制算数右移
 */
infix fun Int.dshr(digit: Int): Int {
    if(digit <= 0)
        return this
    return this / 10f.pow(digit).toInt()
}

/**
 * 十进制算数左移
 */
infix fun Int.dshl(digit: Int): Int {
    if(digit <= 0)
        return this
    return this * (10f.pow(digit).toInt())
}

/**
 * 获取后 [digit] 位数字
 */
fun Int.getEnd(digit: Int): Int {
    if(digit <= 0)
        return this
    return this % (10f.pow(digit).toInt())
}

/**
 * 将后 [digit] 位归零
 */
fun Int.zeroEnd(digit: Int): Int {
    if(digit <= 0)
        return this
    return this - getEnd(digit)
}

//endregion