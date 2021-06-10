package io.github.sunshinewzy.sunnybot.utils

import io.github.sunshinewzy.sunnybot.enums.ServerType
import io.github.sunshinewzy.sunnybot.objects.SBBEServerPing
import io.github.sunshinewzy.sunnybot.objects.SRequest
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import studio.trc.minecraft.serverpinglib.API.MCServerSocket
import studio.trc.minecraft.serverpinglib.Protocol.ProtocolVersion
import java.io.ByteArrayInputStream
import java.lang.Exception

object SServerPing {
    const val apiBEServer = "http://motdpe.blackbe.xyz/api.php"
    
    
    suspend fun Contact.pingServer(serverIp: String, type: ServerType = ServerType.ALL, isDisplayMod: Boolean = false): Message {
        val (ip, port) = serverIp.toIpAndPort() ?: return "������IP��ʽ����".toPlainText()
        
        if(type == ServerType.JAVA_EDITION || type == ServerType.ALL) {
            val socket = MCServerSocket.getInstance(ip, port) ?: return "�޷����ӵ�������".toPlainText()
            var protocolVersion = ProtocolVersion.v1_8_X
            var status = socket.getStatus(protocolVersion) ?: return "�޷���ȡ������״̬".toPlainText()
            if(!status.isMCServer)
                return "�ⲻ��һ��MineCraft������".toPlainText()

            val version = status.version ?: return "��ȡ�������汾ʧ�ܣ�".toPlainText()
            protocolVersion = when {
                version.contains("1.11") -> ProtocolVersion.v1_11
                version.contains("1.12") -> ProtocolVersion.v1_12_2
                version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16") -> ProtocolVersion.v1_13
                else -> ProtocolVersion.v1_8_X
            }

            val exactStatus = socket.getStatus(protocolVersion)
            if(exactStatus != null && exactStatus.isMCServer && exactStatus.version != null) status = exactStatus

            var res =  """
            
            ������IP: $serverIp
            ����IP: ${socket.ip}:${socket.port}
            �������汾: ${status.version}
            ��ǰ��������: ${status.onlinePlayers}
            �����������: ${status.maxPlayers}
            Э��汾: ${status.protocolVersion}
            �ӳ�: ${status.ping}ms
            
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
                            "(Tip: ���� /zt m ����ʾ��ϸmod��Ϣ)"
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

            }

            return text
        }
        
        if(type == ServerType.BEDROCK_EDITION || type == ServerType.ALL) {
            val bean = pingBEServer(serverIp)
            if(bean.status == "online") {
                return """
                    
                    ������IP: $serverIp
                    �������汾: ${bean.version}
                    ��ǰ��������: ${bean.online}
                    �����������: ${bean.max}
                    ��Ϸģʽ: ${bean.gamemode}
                    Э��汾: ${bean.agreement}
                    �ӳ�: ${bean.delay}ms
                    
                    MOTD:
                    ${bean.motd}
                    
                """.trimIndent().toPlainText()
            }
            
            return "�޷����ӵ�������".toPlainText()
        }
        
        return "�ⲻ��һ��MineCraft������".toPlainText()
    }
    
    fun checkServer(serverIp: String): ServerType {
        val (ip, port) = serverIp.toIpAndPort() ?: return ServerType.NOT

        MCServerSocket.getInstance(ip, port)?.let { socket ->
            socket.getStatus(ProtocolVersion.v1_8_X)?.let { status ->
                if(status.isMCServer)
                    return ServerType.JAVA_EDITION
            }
        }
        
        if(pingBEServer(serverIp).status == "online")
            return ServerType.BEDROCK_EDITION
        
        return ServerType.NOT
    }
    
    fun pingBEServer(serverIp: String): SBBEServerPing {
        val (ip, port) = serverIp.toIpAndPort() ?: return SBBEServerPing("", 0, "", "", "", "", "", "", "", "")
        return SRequest(apiBEServer).result(mapOf("ip" to ip, "port" to port))
    }
    
    
    fun String.toIpAndPort(): Pair<String, Int>? {
        val args = split(":")
        if(args.isEmpty() || args.size > 2)
            return null

        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        return ip to port
    }
}