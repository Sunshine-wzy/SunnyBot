package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.commands.SCMiraiCode
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyChannel
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.findIsInstance
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.*

object MessageListener {
    private const val MESSAGE_IMAGES = "MessageImages"
    private val brackets = hashMapOf("(" to ")", "£®" to "£©", "[" to "]", "°æ" to "°ø", "{" to "}", "<" to ">", "°∂" to "°∑")
    private val classLoader = javaClass.classLoader
    private val msgImageList = getMsgImageMap("…œÕº" to "jpg", "≈¿" to "gif")
    private val extensionImage = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
    private val messageImages = getMessageImages()
    

    fun listenMessage() {
        sunnyChannel.subscribeAlways<GroupMessageEvent>(priority = EventPriority.HIGHEST) {
            antiRecall?.saveMessage(group.id, message)
            
            message.findIsInstance<PlainText>()?.let { text -> 
                val content = text.content
                
                if(content.isNotEmpty()) {
//                    val endStr = content.substring(content.length - 1, content.length)

//                    brackets[endStr]?.let {
//                        group.sendMessage(it)
//                    }
                    
                    messageImages[content]?.let {
                        group.sendMessage(QuoteReply(message) + it.uploadAsImage(group))
                    }
                }
                
            }
            
        }

        sunnyChannel.subscribeAlways<MessageRecallEvent.GroupRecall> {
            antiRecall?.antiRecallByGroupEvent(this)
        }
        
        sunnyChannel.subscribeAlways<MessageEvent> { 
            SCMiraiCode.userGetMiraiCode[sender]?.let { time ->
                if(System.currentTimeMillis() - time <= 60_000L) {
                    sender.sendMsg(SCMiraiCode.description, QuoteReply(message) + message.serializeToMiraiCode())
                }
                
                SCMiraiCode.userGetMiraiCode.remove(sender)
            }
        }
        
    }
    
    fun getMessageImages(): HashMap<String, File> {
        val folder = File(PluginMain.dataFolder, MESSAGE_IMAGES)
        if(!folder.exists()) {
            folder.mkdirs()
            
            msgImageList.forEach { pair ->  
                val file = File(folder, pair.first)
                if(!file.exists()) {
                    file.createNewFile()
                }
                
                val output = FileOutputStream(file)
                val bytes = ByteArray(1024)
                val input = pair.second
                
                var index: Int
                do {
                    index = input.read(bytes)
                    output.write(bytes, 0, index)
                } while (index != -1)
                
                output.flush()
                output.close()
                input.close()
            }
        }
        
        val map = hashMapOf<String, File>()
        folder.listFiles()?.forEach { 
            if(it.extension in extensionImage) {
                map[it.nameWithoutExtension] = it
            }
        }
        
        return map
    }
    
    fun getMsgImageMap(vararg name: Pair<String, String>): ArrayList<Pair<String, InputStream>> {
        val list = arrayListOf<Pair<String, InputStream>>()
        name.forEach { pair ->
            val theName = "${pair.first}.${pair.second}"
            classLoader.getResourceAsStream("/$MESSAGE_IMAGES/$theName")?.let {
                list += theName to it
            }
        }
        return list
    }
}