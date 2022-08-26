package io.github.sunshinewzy.sunnybot.objects.internal

import net.mamoe.mirai.message.data.Image

class RequestAddImage(
    val libName: String,
    val imageName: String,
    val message: String,
    val images: List<Image>
) {
    
    companion object {
        val cacheMap: MutableMap<String, RequestAddImage> = hashMapOf()
        
        
        operator fun get(uuid: String): RequestAddImage? = cacheMap[uuid]
    }
    
}