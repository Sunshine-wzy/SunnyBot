package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.enums.SunSTSymbol
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.SDataGroup
import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SSaveGroup
import io.github.sunshinewzy.sunnybot.objects.sDataGroupMap
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.math.pow

//region ���ͺ������ı�

suspend fun Contact.sendMsg(title: Message, text: Message) {
    sendMessage(PlainText("\t��") +
        title + PlainText("��\n") +
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


fun CommandSender.sendMsg(title: Message, text: Message) {
    sunnyScope.launch {
        sendMessage(PlainText("\t��") +
            title + PlainText("��\n") +
            text
        )
    }
}

fun CommandSender.sendMsg(title: String, text: String) {
    sendMsg(PlainText(title), PlainText(text))
}

fun CommandSender.sendMsg(title: String, text: Message) {
    sendMsg(PlainText(title), text)
}

fun CommandSender.sendMsg(title: Message, text: String) {
    sendMsg(title, PlainText(text))
}

fun CommandSender.sendMsg(text: Message) {
    sunnyScope.launch {
        sendMessage(text)
    }
}

fun CommandSender.sendMsg(text: String) {
    sendMsg(PlainText(text))
}

//endregion


//region ����SGroupGameEvent�¼�

fun Member.toSGroupGameEvent(message: MessageChain): SGroupGameEvent {
    val group = this.group
    val groupId = group.id
    if(!SSaveGroup.sGroupMap.containsKey(groupId))
        SSaveGroup.sGroupMap[groupId] = SGroup(groupId)
    val sGroup = SSaveGroup.sGroupMap[groupId]!!
    if(!sDataGroupMap.containsKey(groupId))
        sDataGroupMap[groupId] = SDataGroup()
    val sDataGroup = sDataGroupMap[groupId]!!
    val msg = message.findIsInstance<PlainText>()?.contentToString() ?: ""
    
    return SGroupGameEvent(this, group, groupId, sGroup, sDataGroup, msg)
}

fun Member.toSGroupGameEvent(): SGroupGameEvent {
    return toSGroupGameEvent(PlainText("") + PlainText(""))
}

//endregion


//region ʮ����״̬ѹ��

/**
 * ��ȡָ��λ�õ�����
 */
fun Int.getDigit(digit: Int): Int {
    val str = toString()
    if(str.length < digit){
        return this
    }
    
    return str[digit - 1].toInt()
}

/**
 * �޸�ָ��λ�õ�����
 * @param digit λ
 * @param num �޸ĺ������
 */
fun Int.setDigit(digit: Int, num: Int): Int {
    var ans = zeroEnd(digit) dshr digit - 1
    ans += num
    ans = ans dshl digit - 1
    ans += getEnd(digit - 1)
    return ans
}

/**
 * ʮ������������
 */
infix fun Int.dshr(digit: Int): Int {
    if(digit <= 0)
        return this
    return this / 10f.pow(digit).toInt()
}

/**
 * ʮ������������
 */
infix fun Int.dshl(digit: Int): Int {
    if(digit <= 0)
        return this
    return this * (10f.pow(digit).toInt())
}

/**
 * ��ȡ�� [digit] λ����
 */
fun Int.getEnd(digit: Int): Int {
    if(digit <= 0)
        return this
    return this % (10f.pow(digit).toInt())
}

/**
 * ���� [digit] λ����
 */
fun Int.zeroEnd(digit: Int): Int {
    if(digit <= 0)
        return this
    return this - getEnd(digit)
}

//endregion


//region SunSTSymbol

fun String.newSunSTSymbol(symbol: SunSTSymbol): String = replace(symbol.oldContent, symbol.newContent)

fun String.oldSunSTSymbol(symbol: SunSTSymbol): String = replace(symbol.newContent, symbol.oldContent)

//endregion


//region SunnyAdmin

fun User.isSunnyAdmin(): Boolean = sunnyAdmins.contains(id)

//endregion


//region Usage

infix fun String.usageWith(usage: String): String = "$this\n\n$usage"

//endregion

//region Image

fun BufferedImage.toInputStream(): InputStream? {
    val os = ByteArrayOutputStream()
    try {
        ImageIO.write(this, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
    }
    
    return null
}

//endregion

//region String

/**
 * �ж��Ƿ�Ϊ����
 * @param str ������ַ���
 * @return ����������true,���򷵻�false
 */
fun String.isInteger(): Boolean {
    val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
    return pattern.matcher(this).matches()
}

fun String.isLetterDigitOrChinese(): Boolean {
    val regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$".toRegex()
    return matches(regex)
}

//endregion

//region Char

fun Char.isChineseChar(): Boolean =
    toString().matches("[\u4e00-\u9fa5]".toRegex())

//endregion

//region Map

fun List<Pair<String, String>>.toCommandParams(): String {
    var str = "�������:"
    forEach { 
       str += "\n${it.first}  -  ${it.second}" 
    }
    return str
}

//endregion

//region MessageChain

fun <L: MutableList<String>> MessageChain.getPlainText(list: L): L {
    forEach { 
        if(it is PlainText) {
            list += it.content
        }
    }
    return list
}

//endregion