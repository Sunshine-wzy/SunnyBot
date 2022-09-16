package io.github.sunshinewzy.sunnybot

import io.github.sunshinewzy.sunnybot.enums.SunSTSymbol
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.math.pow

//region 发送含标题文本

suspend fun Contact.sendMsg(title: Message, text: Message) {
    val contact = if(this is Member) group else this
    contact.sendMessage(PlainText("\t『") +
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

fun Contact.sendMessage(title: Message, text: Message) {
    sunnyScope.launch {
        val contact = if(this is Member) group else this@sendMessage
        contact.sendMessage(PlainText("\t『") +
            title + PlainText("』\n") +
            text
        )
    }
}

fun Contact.sendMessage(title: String, text: String) {
    sendMessage(PlainText(title), PlainText(text))
}

fun Contact.sendMessage(title: String, text: Message) {
    sendMessage(PlainText(title), text)
}

fun Contact.sendMessage(title: Message, text: String) {
    sendMessage(title, PlainText(text))
}


fun CommandSender.sendMsg(title: Message, text: Message) {
    sunnyScope.launch {
        sendMessage(PlainText("\t『") +
            title + PlainText("』\n") +
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

suspend fun BufferedImage.uploadAsImage(contact: Contact): Image? {
    toInputStream()?.use { 
        return it.uploadAsImage(contact)
    }
    
    return null
}

//endregion

//region String

/**
 * 判断是否为整数
 * @return 是整数返回true,否则返回false
 */
fun String.isInteger(): Boolean {
    if(this == "") return false
    return Pattern.compile("^[-\\+]?[\\d]*$").matcher(this).matches()
}

fun String.isLetterDigitOrChinese(): Boolean {
    val regex = "^[a-z\\dA-Z\u4e00-\u9fa5]+$".toRegex()
    return matches(regex)
}

fun String.isLegalFileName(): Boolean {
    val str = this.uppercase()
    return !SunnyBot.illegalFileName.contains(str)
}

fun String.removeColor(): String =
    replace("§[\\da-z]".toRegex(), "")

//endregion

//region Char

fun Char.isChineseChar(): Boolean =
    toString().matches("[\u4e00-\u9fa5]".toRegex())

//endregion

//region List

fun List<Pair<String, String>>.toCommandParams(): String {
    var str = "命令参数:"
    forEach { 
       str += "\n${it.first}  -  ${it.second}" 
    }
    return str
}

//endregion

//region Map

fun <K, T> MutableMap<K, MutableList<T>>.putElement(key: K, element: T) {
    val value = this[key]
    if(value != null) {
        value += element
    } else this[key] = arrayListOf(element)
}

fun <K, T> MutableMap<K, MutableList<T>>.removeElement(key: K, element: T) {
    this[key]?.remove(element)
}

fun <K, T> MutableMap<K, MutableList<T>>.clearAndPutElement(key: K, element: T) {
    val value = this[key]
    if(value != null) {
        value.clear()
        value += element
    } else this[key] = arrayListOf(element)
}

fun <K, T> MutableMap<K, MutableSet<T>>.putElementInSet(key: K, element: T) {
    val value = this[key]
    if(value != null) {
        value += element
    } else this[key] = hashSetOf(element)
}

fun <K, T> MutableMap<K, MutableSet<T>>.removeElementInSet(key: K, element: T) {
    this[key]?.remove(element)
}

//endregion

//region MessageChain

fun <L: MutableList<String>> MessageChain.getPlainTextContents(list: L): L {
    forEach { 
        if(it is PlainText) {
            list += it.content
        }
    }
    return list
}

fun MessageChain.getPlainTextContents(): List<String> {
    return LinkedList<String>().also { getPlainTextContents(it) }
}

fun MessageChain.getPlainTextContent(): String {
    return buildString {
        this@getPlainTextContent.forEach {
            if(it is PlainText) {
                append(it.content)
            }
        }
    }
}

//endregion

//region Bot

fun Bot.getUser(id: Long): User? {
    getFriend(id)?.let { return it }
    getStranger(id)?.let { return it }
    return null
}

//endregion

//region Stream

fun InputStream.copyToFile(file: File): Boolean {
    return try {
        file.createFile()
        
        val fos = FileOutputStream(file)
        copyTo(fos)
        fos.close()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        false
    }
}

//endregion

//region File

@Throws(IOException::class)
fun File.createFile() {
    parentFile?.let { 
        if(!it.exists()) {
            it.mkdirs()
        }
    }
    
    createNewFile()
}

//endregion