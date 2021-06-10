package io.github.sunshinewzy.sunnybot.objects

import com.google.gson.Gson
import io.github.sunshinewzy.sunnybot.isChineseChar
import io.github.sunshinewzy.sunnybot.toInputStream
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.imageio.ImageIO


/**
 * @param url 接口地址
 */

class SRequest(private val url: String) {
    private val encodeUrl = urlEncode()
    
    
    fun result(): String {
        return httpRequest()
    }
    
    inline fun <reified T: SBean> result(params: Map<String, Any> = emptyMap()): T {
        if(params.isEmpty()){
            return Gson().fromJson(httpRequest(), T::class.java)
        }
        
        return Gson().fromJson(httpRequest(params), T::class.java)
    }
    
    
    fun resultImage(contact: Contact): Image? {
        var image: Image? = null
        
        try {
            val bufImg = ImageIO.read(URL(url))
            runBlocking { 
                image = bufImg?.toInputStream()?.uploadAsImage(contact)
            }
        } catch (ex: Exception) {
            
        }
        
        return image
    }

    fun resultVoice(contact: Contact): Voice? {
        val theURL = URL(encodeUrl)
        val connection = theURL.openConnection()
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,en-US;q=0.7,en;q=0.6")
        val input = connection.getInputStream()
        
//        val buffer = ByteArray(1204)
//        var byteReader = input.read(buffer)
//        while(byteReader != -1){
//            byteReader = input.read(buffer)
//        }

        var voice: Voice?
        runBlocking {
            val extResource = input.toExternalResource()
            voice = extResource.uploadAsVoice(contact)
            extResource.close()
        }
        return voice
    }
    
    
    fun download(fileOutPath: String, fileName: String) {
        val theURL = URL(encodeUrl)
        val connection = theURL.openConnection()
        val input = connection.getInputStream()
        val output = FileOutputStream("$fileOutPath/$fileName")
        
        val buffer = ByteArray(1204)
        var byteReader = input.read(buffer)
        while(byteReader != -1){
            output.write(buffer, 0, byteReader)
            
            byteReader = input.read(buffer)
        }
        output.close()
    }


    fun httpRequest(): String {
        val request = Request.Builder()
            .url(URL(encodeUrl))
            .build()
        val call = okHttpClient.newCall(request)

        var ans = ""
        try {
            val response = call.execute()
            ans += response.body?.string()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ans
    }
    
    fun httpRequest(params: Map<String, Any>): String {
        val request = Request.Builder()
            .url(URL(encodeUrl + "?" + urlEncode(params)))
            .build()
        val call = okHttpClient.newCall(request)
        
        var ans = ""
        try {
            val response = call.execute()
            ans += response.body?.string()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        
        return ans
    }
    
    fun httpRequestOld(params: Map<String, Any>): String {
        //buffer 用于接收返回的字符
        val buffer = StringBuffer()
        try {
            //建立URL，把请求地址给补全，其中urlEncode()方法用于把params里的参数给取出来
            val theURL = URL(encodeUrl + "?" + urlEncode(params))
            //打开http连接
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,en-US;q=0.7,en;q=0.6")
            httpUrlConn.connect()

            //获得输入
            val inputStream = httpUrlConn.inputStream ?: return ""
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            
            //将bufferReader的值给放到buffer里
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //关闭bufferReader和输入流
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //断开连接
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //返回字符串
        return buffer.toString()
    }

    fun httpRequestOld(): String {
        //buffer 用于接收返回的字符
        val buffer = StringBuffer()
        try {
            //建立URL，把请求地址给补全，其中urlEncode()方法用于把params里的参数给取出来
            val theURL = URL(encodeUrl)
            //打开http连接
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,en-US;q=0.7,en;q=0.6")
            httpUrlConn.connect()

            //获得输入
            val inputStream= httpUrlConn.inputStream ?: return ""
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)

            //将bufferReader的值给放到buffer里
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //关闭bufferReader和输入流
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //断开连接
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //返回字符串
        return buffer.toString()
    }
    
    fun urlEncode(data: Map<String, Any>): String {
        //将map里的参数变成像 showapi_appid=###&showapi_sign=###&的样子
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
    
    fun urlEncode(): String {
        var resultURL = ""
        for(i in url.indices) {
            val charAt = url[i]
            //只对汉字处理
            if(charAt.isChineseChar()) {
                val encode = URLEncoder.encode(charAt.toString(), "UTF-8")
                resultURL += encode
            } else {
                resultURL += charAt
            }
        }
        return resultURL
    }


    fun resultRoselle(serverAddr: String, showFavicon: Int): RosellemcServerInfo {
        //params用于存储要请求的参数
        val params = HashMap<String, Any>()
        params["server_addr"] = serverAddr
        params["show_favicon"] = showFavicon
        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        val strRequest = httpRequest(params)
        //处理返回的JSON数据并返回
        return Gson().fromJson(strRequest, RosellemcServerInfo::class.java)
    }
    
    
    companion object {
        private val okHttpClient = OkHttpClient()
        
    }
}