package io.github.sunshinewzy.sunnybot.commands.command

import io.github.sunshinewzy.sunnybot.PluginMain
import io.github.sunshinewzy.sunnybot.commands.processSCommand
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyScope
import io.github.sunshinewzy.sunnybot.uploadAsImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

object SCGrayScaleImage : RawCommand(
    PluginMain,
    "GrayScaleImage", "gsi",
    usage = "灰度图像处理", description = "灰度图像处理",
    parentPermission = PluginMain.PERM_EXE_USER
) {

    override suspend fun CommandSender.onCommand(args: MessageChain) {
        val subject = subject ?: return

        val argImage = args.findIsInstance<Image>() ?: kotlin.run {
            sendMsg(description, "请输入要进行灰度图像处理的图片")
            return
        }

        sunnyScope.launch {
            withContext(Dispatchers.IO) {
                val queryUrl = argImage.queryUrl()
                val byteStream = SRequest(queryUrl).resultByteStream() ?: run {
                    sendMsg(description, "图片下载链接获取失败")
                    return@withContext
                }

                runCatching {
                    byteStream.use {
                        ImageIO.read(it)
                    }
                }.onFailure {
                    sendMsg(description, "图片下载失败")
                }.onSuccess { rawImage ->
                    val grayImage = rawImage.toGray()

                    withContext(Dispatchers.Default) {
                        processSCommand(args) {
                            "gray" {
                                empty {
                                    sunnyScope.launch {
                                        grayImage.uploadAsImage(subject)?.sendTo(subject)
                                    }
                                }
                            }

                            "binary" {
                                empty {
                                    var startTime = System.nanoTime()
                                    val otsuThreshold = grayImage.otsuThreshold()
                                    val otsuTime = System.nanoTime() - startTime
                                    
                                    startTime = System.nanoTime()
                                    val optimizedOtsuThreshold = grayImage.optimizedOtsuThreshold()
                                    val optimizedOtsuTime = System.nanoTime() - startTime

                                    startTime = System.nanoTime()
                                    val dichotomyOtsuThreshold = grayImage.dichotomyOtsuThreshold()
                                    val dichotomyOtsuTime = System.nanoTime() - startTime
                                    
                                    sunnyScope.launch {
                                        grayImage.binaryzation(otsuThreshold.first).uploadAsImage(subject)?.let { otsuImage ->
                                            grayImage.binaryzation(optimizedOtsuThreshold.first).uploadAsImage(subject)?.let { optimizedOtsuImage ->
                                                grayImage.binaryzation(dichotomyOtsuThreshold.first).uploadAsImage(subject)?.let { dichotomyOtsuImage ->
                                                    sendMsg(
                                                        description,
                                                        "最大类间方差法(大津法)耗时: ${otsuTime.toDouble()*1e-6}ms (${otsuThreshold.second}次循环, 阈值: ${otsuThreshold.first})\n".toPlainText() + otsuImage
                                                            + "\n优化大津法耗时: ${optimizedOtsuTime.toDouble()*1e-6}ms (${optimizedOtsuThreshold.second}次循环, 阈值: ${optimizedOtsuThreshold.first})\n".toPlainText() + optimizedOtsuImage
                                                            + "\n优化时间: ${(otsuTime - optimizedOtsuTime).toDouble()*1e-6}ms".toPlainText()
                                                            + "\n二分大津法耗时: ${(dichotomyOtsuTime.toDouble()*1e-6)}ms (${dichotomyOtsuThreshold.second}次递归, 阈值: ${dichotomyOtsuThreshold.first})\n".toPlainText() + dichotomyOtsuImage
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            "edge" {
                                val edge = grayImage.binaryzation(grayImage.optimizedOtsuThreshold().first).edge()
                                
                                sunnyScope.launch { 
                                    edge.first.uploadAsImage(subject)?.let { image ->
                                        sendMsg(
                                            description,
                                            "八邻域边界检测耗时: ${edge.third*1e-6}ms (种子: (${edge.second.first}, ${edge.second.second}))\n".toPlainText()
                                            + image
                                        )
                                    }
                                }
                            }


                            empty {
                                sendMsg(description, """
                                    > 命令参数
                                    gray  -  灰度图像
                                    binary  -  二值化
                                    edge  -  边界检测
                                """.trimIndent())
                            }
                        }
                    }
                }
            }
        }
    }


    private const val GRAY_SCALE = 256

    
    private fun BufferedImage.edge(): Triple<BufferedImage, Pair<Int, Int>, Long> {
        val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
        val g = image.createGraphics()
        g.drawImage(this, 0, 0, null)
        
        val edgeLeft: LinkedList<Pair<Pair<Int, Int>, Int>> = LinkedList()
        val edgeRight: LinkedList<Pair<Pair<Int, Int>, Int>> = LinkedList()
        val edgeOther: LinkedList<LinkedList<Pair<Pair<Int, Int>, Int>>> = LinkedList()
        
        val startTime = System.nanoTime()
        var leftCoord = -1 to -1
        var leftOffset = 0
        
        val seedLeft = image.edgeSeed(width / 2, height - 4, -1)
        if(seedLeft.first != -1 && seedLeft.second != -1) {
            val result = image.edgeSearch(seedLeft.first, seedLeft.second, edgeLeft, true)
            leftCoord = result.first
            leftOffset = result.second
//            image.spill(seedLeft.first, seedLeft.second, Color(255, 0, 0).rgb)
        }

        var rightCoord = -1 to -1
        var rightOffset = 0
        
        val seedRight = image.edgeSeed(width / 2, height - 4, 1)
        if(seedRight.first != -1 && seedRight.second != -1) {
            val result = image.edgeSearch(seedRight.first, seedRight.second, edgeRight, false)
            rightCoord = result.first
            rightOffset = result.second
//            image.spill(seedRight.first, seedRight.second, Color(255, 0, 0).rgb)
        }
        
        if(leftCoord.first != -1 && leftCoord.second != -1 && leftOffset != 0) {
            image.edgeSearchOther(leftCoord.first, leftCoord.second, leftOffset, edgeOther, true)
        }
        
        if(rightCoord.first != -1 && rightCoord.second != -1 && rightOffset != 0) {
            image.edgeSearchOther(rightCoord.first, rightCoord.second, rightOffset, edgeOther, false)
        }
        
        // 元素识别
        val inflections: LinkedList<Pair<Int, Int>> = LinkedList()
        val leftInflections = searchInflection(edgeLeft)
        val rightInflections = searchInflection(edgeRight)
        val otherInflections: LinkedList<Pair<Int, Int>> = LinkedList()
        edgeOther.forEach { 
            otherInflections += searchInflection(it)
        }
        
        inflections += leftInflections
        inflections += rightInflections
        inflections += otherInflections
        inflections.forEach { 
            image.spill(it.first, it.second, Color(255, 0, 0).rgb)
        }
        
        if(otherInflections.size == 2) {
            
        }
        
        // 中线
        val leftIterator = edgeLeft.iterator()
        val rightIterator = edgeRight.iterator()
        while(leftIterator.hasNext() && rightIterator.hasNext()) {
            val nextLeft = leftIterator.next()
            val nextRight = rightIterator.next()
            
            image.setRGB((nextLeft.first.first + nextRight.first.first) / 2, (nextLeft.first.second + nextRight.first.second) / 2, Color(255, 255, 0).rgb)
        }
        
        return Triple(image, seedLeft, System.nanoTime() - startTime)
    }

    private fun searchInflection(edge: LinkedList<Pair<Pair<Int, Int>, Int>>): LinkedList<Pair<Int, Int>> {
        val inflections: LinkedList<Pair<Int, Int>> = LinkedList()
        val p = ArrayList<Pair<Pair<Int, Int>, Int>>()
        p += edge
        
        val n = p.size
        val m = n / 10
        val c = DoubleArray(n)
        val h = IntArray(n)
        
        for(i in m until n - m) {
            val pi = p[i].first
            val xi = pi.first
            val yi = pi.second
            
            var ckLast = -1.0
            var ckCurrent = -1.0
            
            for(k in m downTo 0) {
                if(k == 0) {
                    ckCurrent = -1.0
                } else {
                    val pik1 = p[i + k].first
                    val pik2 = p[i - k].first

                    val a = (xi - pik1.first) to (yi - pik1.second)
                    val b = (xi - pik2.first) to (yi - pik2.second)

                    ckLast = ckCurrent
                    ckCurrent = (a.first * b.first + a.second * b.second).toDouble() / (sqrt(a.first.toDouble().pow(2) + a.second.toDouble().pow(2)) * sqrt(b.first.toDouble().pow(2) + b.second.toDouble().pow(2)))
                }
                
                if(ckLast >= ckCurrent) {
                    c[i] = ckLast
                    h[i] = k + 1
                    break
                }
            }
        }
        
        for(i in m until n - m) {
            if(c[i] < -0.5) continue
            
            var flag = true
            for(j in 1..h[i]/2) {
                if(c[i] < c[i - j] || c[i] < c[i + j]) {
                    flag = false
                    break
                }
            }
            
            if(flag) {
                inflections += p[i].first
                println(c[i])
            }
        }

        return inflections
    }
    
    private fun tryToSearchElement(edge: LinkedList<Pair<Pair<Int, Int>, Int>>): LinkedList<Pair<Int, Int>> {
        val elements: LinkedList<Pair<Int, Int>> = LinkedList()
        
        val counts = IntArray(9) { 0 }
        val proportions = DoubleArray(9) { 0.0 }
        val cache: LinkedList<Pair<Pair<Int, Int>, Int>> = LinkedList()
        var cnt = 0
        var currentMax = 0
        var lastMax = 0
        val scale = 50
        var currentDirection = 0
        var lastDirection = 0
        var currentS = 0.0
        var lastS = 0.0
        
        for(p in edge) {
            counts[p.second]++
            cache += p

            if(cnt > scale) {
                // 最大生长方向个数法
//                lastMax = currentMax
//                var temp = 0
//                for(i in 1..8) {
//                    if(counts[i] > temp) {
//                        temp = counts[i]
//                        currentMax = i
//                    }
//                }
//                
//                if(currentMax != lastMax) {
//                    elements += cache[SCALE / 2].first
//                }
                
                // 最大生长方向比例法
//                lastDirection = currentDirection
//                var sum = 0
//                for(i in 1..8) {
//                    sum += counts[i]
//                }
//                
//                for(i in 1..8) {
//                    proportions[i] = counts[i].toDouble() / sum
//                }
//                
//                currentDirection = judgeDirection(proportions)
//                if(currentDirection != lastDirection) {
//                    elements += cache[scale / 2].first
//                }
                
                // 二维生长方向方差法
                var n = 0
                var sumX = 0
                var sumY = 0
                var sumSquareX = 0.0
                var sumSquareY = 0.0
                for(i in 1..8) {
                    val count = counts[i]
                    val direction = toDirection(i)
                    
                    n += count
                    
                    sumX += direction.first * count
                    sumY += direction.second * count
                    
                    sumSquareX += direction.first.toDouble().pow(2) * count
                    sumSquareY += direction.second.toDouble().pow(2) * count
                }
                
                lastS = currentS
                val sX = sumSquareX / n - (sumX.toDouble() / n).pow(2)
                val sY = sumSquareY / n - (sumY.toDouble() / n).pow(2)
                
                currentS = sX + sY
                if(currentS > 1.0 || currentS - lastS > 0.1) {
                    elements += cache[scale / 2].first
                }
                
                
                val first = cache.poll()
                if(counts[first.second] > 0)
                    counts[first.second]--
            } else cnt++
        }
        
        return elements
    }

    private fun searchElementLeastSquare(edge: LinkedList<Pair<Pair<Int, Int>, Int>>): LinkedList<Pair<Int, Int>> {
        val elements: LinkedList<Pair<Int, Int>> = LinkedList()

        val counts = IntArray(9) { 0 }
        val cache: LinkedList<Pair<Pair<Int, Int>, Int>> = LinkedList()
        var cnt = 0
        val scale = 30

        for(p in edge) {
            counts[p.second]++
            cache += p

            if(cnt > scale) {
                
                
                val first = cache.poll()
                if(counts[first.second] > 0)
                    counts[first.second]--
            } else cnt++
        }

        return elements
    }
    
    private fun leastSquare() {
        
    }
    
    private fun judgeDirection(proportions: DoubleArray): Int {
        for(i in 1..8) {
            if(proportions[i] > 0.5) {
                if(i % 2 == 0) {
                    val l = offsetLoop(i + 1)
                    val r = offsetLoop(i - 1)
                    
                    if(proportions[l] > proportions[r])
                        return l
                    return r
                }
                
                return i
            }
        }
        
        for(i in 1..8) {
            val l = offsetLoop(i + 1)
            if(proportions[i] + proportions[l] > 0.5) {
                return if(i % 2 != 0) i else l
            }
        }

        return 0
    }
    
    private fun toDirection(offset: Int): Pair<Int, Int> {
        return when(offset) {
            0 -> 0 to 0
            1 -> 1 to 0
            2 -> 1 to 1
            3 -> 0 to 1
            4 -> -1 to 1
            5 -> -1 to 0
            6 -> -1 to -1
            7 -> 0 to -1
            8 -> 1 to -1
            else -> 0 to 0
        }
    }
    
    enum class Direction(val order: Int) {
        NONE(0),
        UP(1),
        DOWN(2),
        LEFT(3),
        RIGHT(4)
    }
    
    private fun BufferedImage.edgeSearch(x: Int, y: Int, edges: LinkedList<Pair<Pair<Int, Int>, Int>>, isLeft: Boolean): Pair<Pair<Int, Int>, Int> {
        var offset = 0
        var coord = x to y
        var p = coord
        
        var cnt = 0

        while(true) {
            var isEdgeExist = false
            for(i in 1..8) {
                if(offset > 8) offset -= 8
                if(offset <= 0) offset += 8
                
                p = if(isLeft) toLeftCoordinate(coord.first, coord.second, offset) else toRightCoordinate(coord.first, coord.second, offset)
                
                if(p.first in 0 until width && p.second in 0 until height) {
                    val color = getColor(p)
                    if(color == 0) {
                        edges += p to offset
                        offset -= 2
                        
                        setRGB(p.first, p.second, Color(1, 255, 0).rgb)
                        
                        cnt++
                        if(cnt > 10) {
                            cnt = 0
                        }
                        
                        coord = p
                        isEdgeExist = true
                        break
                    } else if(color != 255) {
                        break
                    }
                } else {
                    offset -= 2

                    coord = p
                    break
                }
                
                offset++
            }
            
            if(!isEdgeExist) {
                return coord to offset
            }
        }
    }

    private fun BufferedImage.edgeSearchOther(x: Int, y: Int, initialOffset: Int, edges: LinkedList<LinkedList<Pair<Pair<Int, Int>, Int>>>, isLeft: Boolean) {
        var offset = initialOffset
        var coord = x to y
        var p = coord
        var edge: LinkedList<Pair<Pair<Int, Int>, Int>> = LinkedList()

        var cnt = 0

        while(true) {
            var isEdgeExist = false
            for(i in 1..8) {
                if(offset > 8) offset -= 8
                if(offset <= 0) offset += 8

                p = if(isLeft) toLeftCoordinate(coord.first, coord.second, offset) else toRightCoordinate(coord.first, coord.second, offset)

                if(p.first in 0 until width && p.second in 0 until height) {
                    val color = getColor(p)
                    if(color == 0) {
                        edge += p to offset
                        offset -= 2

                        setRGB(p.first, p.second, Color(1, 255, 0).rgb)

                        cnt++
                        if(cnt > 10) {
                            cnt = 0
                        }

                        coord = p
                        isEdgeExist = true
                        break
                    } else if(color != 255) {
                        break
                    }
                } else {
                    if(edge.isNotEmpty()) {
                        edges += edge
                        edge = LinkedList()
                    }

                    offset -= 2

                    coord = p
                    isEdgeExist = true
                    break
                }

                offset++
            }

            if(!isEdgeExist) {
                break
            }
        }
    }
    
    private fun BufferedImage.edgeSearchDfs(x: Int, y: Int, edge: HashSet<Pair<Int, Int>>, flag: HashMap<Pair<Int, Int>, Boolean>, depth: Int) {
        if(depth > 1000) return
        flag[x to y] = true

        for(i in 1..8) {
            val p1 = toRightCoordinate(x, y, i)
            if(flag[p1] == true) continue
            val color1 = getColor(p1)

            val p2 = toRightCoordinate(x, y, offsetLoop(i + 1))
            val color2 = getColor(p2)

            if(color1 != color2) {
                val next = if(color1 == 255) p1 else p2
                if(flag[next] == true) continue
                
                edgeSearchDfs(next.first, next.second, edge, flag, depth + 1)
            }
        }
    }
    
    private fun offsetLoop(offset: Int): Int {
        if(offset > 8)
            return offset - 8
        if(offset <= 0)
            return offset + 8
        return offset
    }
    
    private fun BufferedImage.edgeSeed(x: Int, y: Int, direction: Int): Pair<Int, Int> {
        var p = x
        while(p in 0 until width) {
            if(getColor(p, y) == 0)
                return p to y
            
            p += direction
        }
        
        return -1 to -1
    }
    
    private fun BufferedImage.edgeSeedBfs(x: Int, y: Int): Pair<Int, Int> {
        val flag = hashMapOf<Pair<Int, Int>, Boolean>()
        val queue = LinkedList<Pair<Int, Int>>()

        val origin = x to y
        flag[origin] = true
        setRGB(x, y, Color(0, 255, 255).rgb)
        queue.offer(origin)
        
//        var cnt = 0
        while(queue.isNotEmpty()) {
//            cnt++
            
            val coord = queue.poll()
            val color = getColor(coord.first, coord.second)
            
            setRGB(coord.first, coord.second, Color(0, 255, 0).rgb)
//            println("> $cnt: ${queue.size + 1}\n$coord: $color\n")
            
            if(color == 255) {
                return coord
            } else if(color == -1) {
                continue
            }
            
            for(i in 1..8) {
                val iCoord = toRightCoordinate(coord.first, coord.second, i)
                if(flag[iCoord] == true || iCoord.first < 0 || iCoord.second < 0 || iCoord.first >= width || iCoord.second >= height) {
                    continue
                }

                flag[iCoord] = true
                queue.offer(iCoord)
            }
        }
        
        return -1 to -1
    }
    
    private fun BufferedImage.edgeSeedDfs(x: Int, y: Int, flag: HashMap<Pair<Int, Int>, Boolean> = hashMapOf(), cnt: Int = 0): Pair<Int, Int> {
        if(cnt > 1000) return -1 to -1
        
        for(i in 0..8) {
            val coord = toRightCoordinate(x, y, i)
            if(flag[coord] == true) continue
            
            val color = getColor(coord.first, coord.second)
            if(color == 255) {
                return coord
            } else if(color == -1) {
                continue
            }
            
            flag[coord] = true
            setRGB(coord.first, coord.second, Color(0, 255, 0).rgb)
            val pair = edgeSeedDfs(coord.first, coord.second, flag, cnt + 1)
            if(pair.first != -1 && pair.second != -1)
                return pair
        }
        
        return -1 to -1
    }
    
    
    private const val SPILL_SCALE = 3
    
    private fun BufferedImage.spill(x: Int, y: Int, rgb: Int) {
        for(i in -SPILL_SCALE..SPILL_SCALE) {
            for(j in -SPILL_SCALE..SPILL_SCALE) {
                val theX = x + i
                val theY = y + j
                if(theX < 0 || theY < 0 || theX >= width || theY >= height)
                    continue
                
                setRGB(theX, theY, rgb)
            }
        }
    }

    private fun toLeftCoordinate(x: Int, y: Int, offset: Int): Pair<Int, Int> {
        return when(offset) {
            0 -> x to y
            1 -> (x + 1) to y
            2 -> (x + 1) to (y - 1)
            3 -> x to (y - 1)
            4 -> (x - 1) to (y - 1)
            5 -> (x - 1) to y
            6 -> (x - 1) to (y + 1)
            7 -> x to (y + 1)
            8 -> (x + 1) to (y + 1)
            else -> x to y
        }
    }
    
    private fun toRightCoordinate(x: Int, y: Int, offset: Int): Pair<Int, Int> {
        return when(offset) {
            0 -> x to y
            1 -> (x - 1) to y
            2 -> (x - 1) to (y - 1)
            3 -> x to (y - 1)
            4 -> (x + 1) to (y - 1)
            5 -> (x + 1) to y
            6 -> (x + 1) to (y + 1)
            7 -> x to (y + 1)
            8 -> (x - 1) to (y + 1)
            else -> x to y
        }
    }

    private fun BufferedImage.getColor(x: Int, y: Int): Int {
        if(x < 0 || y < 0 || x >= width || y >= height)
            return -1

        return Color(getRGB(x, y)).red
    }
    
    private fun BufferedImage.getColor(coord: Pair<Int, Int>): Int {
        return getColor(coord.first, coord.second)
    }
    
    private fun BufferedImage.getColor(x: Int, y: Int, offset: Int): Int {
        val coord = toRightCoordinate(x, y, offset)
        
        return getColor(coord.first, coord.second)
    }

    private fun BufferedImage.toGray(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
        for(y in 0 until height) {
            for(x in 0 until width) {
                val color = Color(getRGB(x, y))
                val gray = (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114).toInt()
                val newColor = Color(gray, gray, gray)
                image.setRGB(x, y, newColor.rgb)
            }
        }
        return image
    }

    private fun BufferedImage.otsuThreshold(): Pair<Int, Int> {
        val pixelCount = IntArray(GRAY_SCALE)
        val pixelPro = DoubleArray(GRAY_SCALE)
        val pixelSum = height * width
        var threshold = 0

        //统计灰度级中每个像素在整幅图像中的个数
        for(y in 0 until height) {
            for(x in 0 until width) {
                val color = Color(getRGB(x, y))
                val gray = color.red

                //将像素值作为计数数组的下标
                pixelCount[gray]++
            }
        }

        //计算每个像素值的点在整幅图像中的比例
        var maxPro = 0.0
        for(i in pixelPro.indices) {
            pixelPro[i] = pixelCount[i].toDouble() / pixelSum
            if(pixelPro[i] > maxPro) {
                maxPro = pixelPro[i]
            }
        }

        //遍历灰度级[0,255]
        var w0 = 0.0
        var w1 = 0.0
        var m0tmp = 0.0
        var m1tmp = 0.0
        var m0 = 0.0
        var m1 = 0.0
        var m = 0.0
        var sTmp = 0.0
        var sMax = 0.0
        var cnt = 0

        //i作为阈值
        for(i in 30 until 200) {
            cnt++
            
            w0 = 0.0
            w1 = 0.0
            m0tmp = 0.0
            m1tmp = 0.0

            //计算阈值i的类间方差
            for(j in pixelPro.indices) {
                //灰度值小于等于 i 的作为背景部分
                if(j <= i) {
                    //背景部分每个灰度值的像素点所占比例之和 即背景部分的比例
                    w0 += pixelPro[j]
                    //背景部分 每个灰度值*占总的比例
                    m0tmp += j * pixelPro[j]
                } else {
                    //前景部分
                    w1 += pixelPro[j]
                    m1tmp += j * pixelPro[j]
                }
            }

            //背景平均灰度
            m0 = m0tmp / w0
            //前景平均灰度
            m1 = m1tmp / w1
            //全局平均灰度
            m = m0tmp + m1tmp

            //计算 当前阈值i的类间方差
            sTmp = w0 * (m0 - m).pow(2) + w1 * (m1 - m).pow(2)

            //比较出类间方差最大的阈值i
            if(sTmp > sMax) {
                sMax = sTmp
                threshold = i
            }
        }

        return threshold to cnt
    }

    private fun BufferedImage.optimizedOtsuThreshold(): Pair<Int, Int> {
        val m = DoubleArray(GRAY_SCALE)
        val p = DoubleArray(GRAY_SCALE)
        val pixelCount = IntArray(GRAY_SCALE)
        val pixelPro = DoubleArray(GRAY_SCALE)
        val pixelSum = height * width
        val threshold = IntArray(2)

        //统计灰度级中每个像素在整幅图像中的个数
        for(y in 0 until height) {
            for(x in 0 until width) {
                val color = Color(getRGB(x, y))
                val gray = color.red

                //将像素值作为计数数组的下标
                pixelCount[gray]++
            }
        }

        var mSum = 0.0
        var pSum = 0.0
        //计算每个像素值的点在整幅图像中的比例
        for(i in 0 until GRAY_SCALE) {
            pixelPro[i] = pixelCount[i].toDouble() / pixelSum

            mSum += i * pixelPro[i]
            pSum += pixelPro[i]
            
            m[i] = mSum
            p[i] = pSum
        }

        //遍历灰度级[0,255]
        val sTmp = DoubleArray(2)
        val sCache = DoubleArray(2)
        val sMax = DoubleArray(2)
        val trend = BooleanArray(2)
        var cnt = 1

        //i作为阈值
        for(k in 0 until 128) {

            if(k == 0) {
                val tmp = interclassVariance(128, p, m)
                
                for(j in 0..1) {
                    sTmp[j] = tmp
                    sCache[j] = tmp
                }
            } else {
                for(j in 0..1) {
                    cnt++
                    
                    val i = if(j == 0) 128 + k else 128 - k
                    sTmp[j] = interclassVariance(i, p, m)
//                    if(j == 1) {
//                        sunnyBot.logger.info(sTmp[j].toString())
//                    } else sunnyBot.logger.warning(sTmp[j].toString())
                    
                    if(k == 1) {
                        if(sTmp[j] > sCache[j]) {
                            trend[j] = true
                        }
                    } else if(!trend[j]) {
                        if(sTmp[j] > sCache[j]) {
                            trend[j] = true
                        }
                    }
                    
                    if(trend[j] && sTmp[j] < sCache[j])
                        return threshold[j] to cnt
                    
                    if(sTmp[j] > sMax[j]) {
                        sMax[j] = sTmp[j]
                        threshold[j] = i
                    }
                    
                    sCache[j] = sTmp[j]
                }
            }
        }

        return threshold[0] to cnt
    }
    
    private fun BufferedImage.dichotomyOtsuThreshold(): Pair<Int, Int> {
        val m = DoubleArray(GRAY_SCALE)
        val p = DoubleArray(GRAY_SCALE)
        val pixelCount = IntArray(GRAY_SCALE)
        val pixelPro = DoubleArray(GRAY_SCALE)
        val pixelSum = height * width

        //统计灰度级中每个像素在整幅图像中的个数
        for(y in 0 until height) {
            for(x in 0 until width) {
                val color = Color(getRGB(x, y))
                val gray = color.red

                //将像素值作为计数数组的下标
                pixelCount[gray]++
            }
        }

        var mSum = 0.0
        var pSum = 0.0
        //计算每个像素值的点在整幅图像中的比例
        for(i in 0 until GRAY_SCALE) {
            pixelPro[i] = pixelCount[i].toDouble() / pixelSum

            mSum += i * pixelPro[i]
            pSum += pixelPro[i]

            m[i] = mSum
            p[i] = pSum
        }

        val cnt = IntArray(1)
        val pair = dichotomyOtsu(0, 255, p, m, cnt)
        return pair.second to cnt[0]
    }
    
    private fun dichotomyOtsu(start: Int, end: Int, p: DoubleArray, m: DoubleArray, cnt: IntArray): Pair<Double, Int> {
        cnt[0]++
        
        val mid = (start + end) / 2
        val sMid = interclassVariance(mid, p, m)
        if(cnt[0] > 200 || start == end || start == mid || end == mid) return Pair(sMid, mid)
        
        val sMidRight = interclassVariance(mid + 1, p, m)
        val sMidLeft = interclassVariance(mid - 1, p, m)
        
        if(sMid > sMidLeft && sMid > sMidRight) {
            return Pair(sMid, mid)
        }
        
        val left = dichotomyOtsu(start, mid, p, m, cnt)
        val right = dichotomyOtsu(mid + 1, end, p, m, cnt)
        
        return if(left.first > right.first) {
            left
        } else {
            right
        }
    }
    
    private fun interclassVariance(threshold: Int, p: DoubleArray, m: DoubleArray): Double {
        val p0 = p[threshold]
        
        //计算 当前阈值i的类间方差
        return (m[GRAY_SCALE - 1] * p0 - m[threshold]).pow(2) / (p0 * (1 - p0))
    }
    
    private fun BufferedImage.binaryzation(threshold: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

        for(y in 0 until height) {
            for(x in 0 until width) {
                val color = Color(getRGB(x, y))
                val newValue = if(color.red > threshold) 255 else 0
                image.setRGB(x, y, Color(newValue, newValue, newValue).rgb)
            }
        }
        
        return image
    }

}