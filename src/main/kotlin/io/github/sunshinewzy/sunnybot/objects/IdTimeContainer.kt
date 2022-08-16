package io.github.sunshinewzy.sunnybot.objects

class IdTimeContainer(val period: Long = 60_000L) {
    private val ids = HashSet<Long>()
    private var lastTime: Long = 0
    
    
    fun add(id: Long): IdTimeContainer {
        if(ids.isEmpty()) {
            startTimings()
        }
        
        ids += id
        return this
    }
    
    fun remove(id: Long): IdTimeContainer {
        ids -= id
        return this
    }
    
    fun clear() {
        ids.clear()
    }
    
    fun contains(id: Long): Boolean =
        ids.contains(id)
    
    fun time(): Long =
        System.currentTimeMillis() - lastTime
    
    fun timeLeft(): Long {
        val leftTime = period - time()
        if(leftTime > 0)
            return leftTime
        
        return 0
    }
    
    fun checkTimeout(): Boolean {
        val isTimeout = time() >= period
        
        if(isTimeout) {
            timeout()
        }
        
        return isTimeout
    }
    
    fun update() {
        lastTime = System.currentTimeMillis()
    }
    
    
    private fun startTimings() {
        lastTime = System.currentTimeMillis()
    }
    
    private fun timeout() {
        lastTime = 0
        clear()
    }
    
}