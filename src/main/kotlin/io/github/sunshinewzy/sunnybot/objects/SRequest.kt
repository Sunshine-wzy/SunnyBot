package io.github.sunshinewzy.sunnybot.objects

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/**
 * @param url �ӿڵ�ַ
 */

class SRequest(private val url: String) {
    fun result(): String {
        return httpRequest()
    }
    
    fun resultRoselle(serverAddr: String, showFavicon: Int): RosellemcServerInfo {
        //params���ڴ洢Ҫ����Ĳ���
        val params = HashMap<String, Any>()
        params["server_addr"] = serverAddr
        params["show_favicon"] = showFavicon
        //����httpRequest���������������Ҫ���������ַ���������������
        val strRequest = httpRequest(params)
        //�����ص�JSON���ݲ�����
        return Gson().fromJson(strRequest, RosellemcServerInfo::class.java)
    }

    fun resultImage(contact: Contact): Image? {
        var image: Image? = null
        try {
            val theURL = URL(url)
            val httpUrlConn = theURL.openConnection() as HttpURLConnection
            
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.connect()
            
            val inputStream = httpUrlConn.inputStream
            
            runBlocking {
                image = inputStream.uploadAsImage(contact)
            }
            
            inputStream.close()
            httpUrlConn.disconnect()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return image
    }
    
    
    
    private fun httpRequest(params: Map<String, Any>): String {
        //buffer ���ڽ��շ��ص��ַ�
        val buffer = StringBuffer()
        try {
            //����URL���������ַ����ȫ������urlEncode()�������ڰ�params��Ĳ�����ȡ����
            val theURL = URL(url + "?" + urlEncode(params))
            //��http����
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.connect()

            //�������
            val inputStream = httpUrlConn.inputStream ?: return ""
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            
            //��bufferReader��ֵ���ŵ�buffer��
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //�ر�bufferReader��������
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //�Ͽ�����
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //�����ַ���
        return buffer.toString()
    }

    private fun httpRequest(): String {
        //buffer ���ڽ��շ��ص��ַ�
        val buffer = StringBuffer()
        try {
            //����URL���������ַ����ȫ������urlEncode()�������ڰ�params��Ĳ�����ȡ����
            val theURL = URL(url)
            //��http����
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.connect()

            //�������
            val inputStream= httpUrlConn.inputStream ?: return ""
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)

            //��bufferReader��ֵ���ŵ�buffer��
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //�ر�bufferReader��������
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //�Ͽ�����
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //�����ַ���
        return buffer.toString()
    }
    
    private fun urlEncode(data: Map<String, Any>): String {
        //��map��Ĳ�������� showapi_appid=###&showapi_sign=###&������
        val sb = StringBuilder()
        for ((key, value) in data.entries) {
            try {
                sb.append(key).append("=").append(URLEncoder.encode(value.toString() + "", "UTF-8")).append("&")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}