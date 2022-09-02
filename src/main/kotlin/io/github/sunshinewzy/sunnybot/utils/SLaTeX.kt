package io.github.sunshinewzy.sunnybot.utils

import io.github.sunshinewzy.sunnybot.uploadAsImage
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.JLabel

object SLaTeX {
    fun generate(formula: String): BufferedImage {
        val tf = TeXFormula(formula)
        val ti = tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40f)
        val bimg = BufferedImage(ti.iconWidth, ti.iconHeight, BufferedImage.TYPE_4BYTE_ABGR)

        val g2d = bimg.createGraphics()
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, ti.iconWidth, ti.iconHeight)
        val jl = JLabel()
        jl.foreground = Color(0, 0, 0)
        ti.paintIcon(jl, g2d, 0, 0)
        
        return bimg
    }
    
    suspend fun Contact.laTeXImage(formula: String): Image? = generate(formula).uploadAsImage(this)
}