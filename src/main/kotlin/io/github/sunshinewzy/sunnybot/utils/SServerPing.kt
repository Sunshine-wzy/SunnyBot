package io.github.sunshinewzy.sunnybot.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import studio.trc.minecraft.serverpinglib.API.MCServerSocket
import studio.trc.minecraft.serverpinglib.Protocol.ProtocolVersion
import java.io.ByteArrayInputStream
import java.lang.Exception

object SServerPing {
    suspend fun Contact.pingServer(serverIp: String, isDisplayMod: Boolean = false): Message {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return "".toPlainText()
        
        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        val socket = MCServerSocket.getInstance(ip, port) ?: return "无法连接到服务器".toPlainText()
        
        var protocolVersion = ProtocolVersion.v1_8_X
        var status = socket.getStatus(protocolVersion) ?: return "无法获取服务器状态".toPlainText()
        if(!status.isMCServer)
            return "这不是一个MineCraft服务器".toPlainText()
        
        val version = status.version ?: return "获取服务器版本失败！".toPlainText()
        protocolVersion = when {
            version.contains("1.11") -> ProtocolVersion.v1_11
            version.contains("1.12") -> ProtocolVersion.v1_12_2
            version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16") -> ProtocolVersion.v1_13
            else -> ProtocolVersion.v1_8_X
        }
        
        val exactStatus = socket.getStatus(protocolVersion)
        if(exactStatus != null && exactStatus.isMCServer && exactStatus.version != null) status = exactStatus
        
        var res =  """
            
            服务器IP: $serverIp
            服务器版本: ${status.version}
            当前在线人数: ${status.onlinePlayers}
            最大在线人数: ${status.maxPlayers}
            协议版本: ${status.protocolVersion}
            
            MOTD:
            ${status.motd.line1MotdText}
            ${status.motd.line2MotdText}
            
        """.trimIndent()

        val info = status.modInfo
        if(info.hasModInfo()){
            if(info.hasMod()){
                if(isDisplayMod){
                    res += "\n>> ${info.modList.size} Mods:\n"
                    info.modList.forEach {
                        res += "[ " + it.modId + " ] v" + it.version + "\n"
                    }
                }
                else{
                    res += "\n>> ${info.modList.size} Mods\n" +
                        "(Tip: 输入 /zt m 以显示详细mod信息)"
                }
            }
        }
        
        val text = res.toPlainText()
        
        try {
            val icon = status.icon ?: return text
            val imageBytes = icon.imageBytes ?: return text
            val inputStream = ByteArrayInputStream(imageBytes)
            val image = inputStream.uploadAsImage(this)

            return image + text
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        
        return text
    }
    
    fun checkServer(serverIp: String): Boolean {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return false

        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()

        val socket = MCServerSocket.getInstance(ip, port) ?: return false

        val status = socket.getStatus(ProtocolVersion.v1_8_X) ?: return false
        if(!status.isMCServer)
            return false
        
        return true
    }
}