package io.github.sunshinewzy.sunnybot.objects.data

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.commands.SCImage
import io.github.sunshinewzy.sunnybot.sendMsg
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File
import java.util.*

@Serializable
class ImageData(
    val name: String,
    val fileNames: MutableList<String> = arrayListOf(),
    val messages: MutableSet<String> = hashSetOf()
) {
    fun addMessage(message: String) {
        val msg = message.lowercase()
        messages += msg
        messageImages[msg] = this
    }
    
    
    suspend fun getImages(contact: Contact): List<Image> {
        if(fileNames.isEmpty()) return emptyList()

        return LinkedList<Image>().also { list ->
            fileNames.forEach { fileName ->
                contact.getImageFromFile(fileName)?.let {
                    list += it
                }
            }
        }
    }
    
    fun remove(): Boolean {
        if(!removeImages()) return false
        
        messages.forEach { 
            removeMessage(it)
        }
        
        return true
    }
    
    fun removeImages(): Boolean {
        fileNames.removeIf { 
            removeImageInFile(it)
        }
        
        return fileNames.isEmpty()
    }
    
    fun removeMessage(message: String) {
        messageImages -= message
        messages -= message
    }
    
    
    companion object {
        val messageImages: MutableMap<String, ImageData> = hashMapOf()


        suspend fun Contact.getImageFromFile(fileName: String): Image? {
            try {
                File(
                    PluginMain.dataFolder,
                    "image/$fileName"
                ).toExternalResource().use {
                    return it.uploadAsImage(this)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                sendMsg(SCImage.description, "Õº∆¨ '$fileName' ªÒ»° ß∞‹")
            }

            return null
        }
        
        fun removeImageInFile(fileName: String): Boolean {
            return try {
                File(
                    PluginMain.dataFolder,
                    "image/$fileName"
                ).delete()
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
        
    }
    
}