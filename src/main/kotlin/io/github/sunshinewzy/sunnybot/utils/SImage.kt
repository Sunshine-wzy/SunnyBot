package io.github.sunshinewzy.sunnybot.utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object SImage {
    private const val folderName = "Image/"
    private const val OFFSET_Y = 50
    
    private val classLoader = javaClass.classLoader
    private val folderPath = classLoader.getResource("Image")?.file ?: throw IllegalArgumentException("Image folder is not found")
    private val folder = File(folderPath)
    
    private val fontBase = Font("微软雅黑", Font.PLAIN, 45)
    private val imageTextureSilver = loadImage("Background/texture_silver.png")
    
    
    fun loadImage(path: String): BufferedImage {
        return ImageIO.read(classLoader.getResource(folderName + path))
            ?: throw IllegalArgumentException("Image $path is not found")
    }
    
    fun loadImageFolder(path: String): ArrayList<BufferedImage> {
        val list = ArrayList<BufferedImage>()

        val file = File(folder, path)
        file.listFiles()?.forEach { 
            ImageIO.read(it)?.let { img ->
                list += img
            }
        }
        
        return list
    }
    
    fun loadImageFolder(path: String, st: Char, ed: Char, suffix: String = "png"): ArrayList<BufferedImage> {
        val list = ArrayList<BufferedImage>()
        
        for(ch in st..ed){
            classLoader.getResource("$folderName$path/$ch.$suffix")?.let { 
                ImageIO.read(it)?.let { img ->
                    list += img
                }
            }
        }
        
        return list
    }


    fun Graphics2D.drawText(content: String, x: Int, y: Int, font: Font, color: Color = Color.BLACK) {
        this.color = color
        this.font = font
        
        var i = 0
        var j = 0
        var count = 0
        while(content.indexOf('\n', j).also { i = it } != -1) {
            drawString(content.substring(j, i), x, y + OFFSET_Y * count)
            j = i + 1
            count++
        }
    }
    
    
    fun showTextWithSilverBackground(content: String): BufferedImage {
        val lineCount = content.count { it == '\n' } + 1
        val height = OFFSET_Y * lineCount
        
        val image = BufferedImage(imageTextureSilver.width, height, imageTextureSilver.type)
        val g = image.createGraphics()
        
        g.drawImage(imageTextureSilver, 0, 0, imageTextureSilver.width, height, null)
        g.drawText(content, 100, 100, fontBase)
        
        g.dispose()
        return image
    }
    
}