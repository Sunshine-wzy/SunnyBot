package io.github.sunshinewzy.sunnybot.utils

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object SImage {
    private const val folderName = "Image/"
    private val classLoader = javaClass.classLoader
    private val folderPath = classLoader.getResource("Image")?.file ?: throw IllegalArgumentException("Image folder is not found")
    private val folder = File(folderPath)
    
    
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
    
}