package io.github.sunshinewzy.sunnybot.objects.data

import io.github.sunshinewzy.sunnybot.commands.SCImage
import io.github.sunshinewzy.sunnybot.objects.SunnyData
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image

@Serializable
class ImageLibraryData(
    val name: String,
    val imageMap: MutableMap<String, ImageData> = hashMapOf(),
    val aliases: MutableSet<String> = hashSetOf()
) {
    
    fun addImage(imageName: String, fileName: String) {
        imageMap.getOrPut(imageName) { ImageData(imageName) }.fileNames += fileName
    }
    
    fun getImageData(contact: Contact, imageName: String): ImageData? {
        return imageMap[imageName] ?: kotlin.run {
            sunnyScope.launch { contact.sendMsg(SCImage.description, "Í¼¿â '$name' ÖÐÃ»ÓÐÍ¼Æ¬ '$imageName'") }
            null
        }
    }
    
    fun remove(): Boolean {
        if(!removeImages()) return false
        
        SunnyData.image -= name
        SCImage.removeLibraryMap(this)
        return true
    }

    fun removeImage(imageName: String): Boolean {
        val imageData = imageMap[imageName] ?: return true
        if(!imageData.remove()) return false
        
        imageMap -= imageName
        return true
    }
    
    fun removeImages(): Boolean {
        var flag = true
        val removeKeys = hashSetOf<String>()
        
        imageMap.forEach { (imageName, imageData) -> 
            if(imageData.remove()) {
                removeKeys += imageName
            } else {
                flag = false
            }
        }
        
        removeKeys.forEach { 
            imageMap -= it
        }
        
        return flag
    }
    
    
    suspend fun getImages(contact: Contact, imageName: String): List<Image> {
        val data = imageMap[imageName] ?: return emptyList()
        return data.getImages(contact)
    }
    
}