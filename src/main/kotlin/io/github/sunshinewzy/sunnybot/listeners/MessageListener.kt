package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.commands.SCImage
import io.github.sunshinewzy.sunnybot.commands.SCMiraiCode
import io.github.sunshinewzy.sunnybot.commands.SCommandManager
import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.SBOwnThink
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.getSData
import io.github.sunshinewzy.sunnybot.objects.setRunningState
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.findIsInstance
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MessageListener {
    private const val MESSAGE_IMAGES = "MessageImages"
    private val brackets = hashMapOf("(" to ")", "��" to "��", "[" to "]", "��" to "��", "{" to "}", "<" to ">", "��" to "��")
    private val classLoader = javaClass.classLoader
    private val msgImageList = getMsgImageMap("��ͼ" to "jpg", "��" to "gif")
    private val extensionImage = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
    private val messageImages = getMessageImages()
    

    fun listenMessage() {
        sunnyChannel.subscribeMessages {

            (contains("���Ӳ���")) end@{
                val group = subject as? Group ?: return@end
                val data = group.getSData()

                val state = data.runningState
                if(state == RunningState.FREE) {
                    subject.sendMsg("Game", "��ǰû����Ϸ���ڽ��С�")
                    return@end
                }

                val players = data.players
                if(players.contains(sender.id) || players.checkTimeout()) {
                    group.setRunningState(RunningState.FREE)
                    subject.sendMsg("Game", "${state.gameName} ��Ϸ����")
                } else {
                    subject.sendMsg("Game", """
                    �����ǵ�ǰ��Ϸ�����
                    �� ${players.timeLeft()}���� ����ܽ�����ǰ��Ϸ
                """.trimIndent())
                }

            }

            (contains("�����ڰ�")) startAgain@{
                val member = sender as? Member ?: return@startAgain
                val group = member.group
                val data = group.getSData()

                val state = data.runningState
                val lastState = data.lastRunningState
                if(state == RunningState.FREE) {
                    if(lastState != RunningState.FREE) {
                        SGameManager.callGame(member, lastState.gameName, true)
                    } else {
                        group.sendMsg("Game", "û����Ϸ��¼")
                    }

                    return@startAgain
                }

                val players = data.players
                if(players.contains(sender.id) || players.checkTimeout()) {
                    SGameManager.callGame(member, state.gameName, true)
                } else {
                    subject.sendMsg("Game", """
                    �����ǵ�ǰ��Ϸ�����
                    �� ${players.timeLeft()}���� ����ܽ�����ǰ��Ϸ
                """.trimIndent())
                }

            }
            
            startsWith("#", removePrefix = true, trim = true) {
                SCommandManager.executeCommand(toCommandSender(), message, it)
            }
            
            startsWith("#����", removePrefix = true, trim = true) { libName ->
                SCImage.sendRandomImage(sender, libName)
            }



//        (contains("sunny", ignoreCase = true) and (contains("����") or contains("˯��") or contains("��"))) sleep@{
//            if(sender !is Member)
//                return@sleep
//            val group = getGroup(sender) ?: return@sleep
//            
//            group.getSGroup().isOpen = false
//            group.sendMessage("Bye~ master")
//        }
//        
//        (contains("sunny", ignoreCase = true) and (contains("����") or contains("��") or contains("��"))) start@{
//            if(sender !is Member)
//                return@start
//            val group = getGroup(sender) ?: return@start
//            
//            group.getSGroup().isOpen = true
//            group.sendMessage("Hi! master")
//        }

            atBot {
                val member = sender as? Member ?: return@atBot
                val text = message.findIsInstance<PlainText>() ?: kotlin.run {
                    member.group.sendIntroduction()
                    return@atBot
                }
                val msg = text.content.replace("\n", "").replace(" ", "")
                if(msg.isEmpty()) {
                    member.group.sendIntroduction()
                    return@atBot
                }

                val ownThink = SRequest("https://api.ownthink.com/bot?spoken=$msg")
                    .resultBean<SBOwnThink>()
                if(ownThink.message == "success") {
                    member.group.sendMessage(QuoteReply(message) + At(member) + " ${ownThink.data.info.text}")
                } else member.group.sendMessage("˼ �� �� ��")

            }

        }
        
        
        sunnyChannel.subscribeAlways<GroupMessageEvent>(priority = EventPriority.HIGHEST) {
            antiRecall?.saveMessage(group.id, message)
            
            message.findIsInstance<PlainText>()?.let { text -> 
                val content = text.content
                
                if(content.isNotEmpty()) {
                    // ���Ų�ȫ
//                    val endStr = content.substring(content.length - 1, content.length)
//                    brackets[endStr]?.let {
//                        group.sendMessage(it)
//                    }
                    
                    // ��ͼ
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
            
            it.message
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