package io.github.sunshinewzy.sunnybot.objects

class IdTimeContainer(val period: Long = 60_000L) {
    private val ids = HashSet<Long>()
    private var time: Long = 0
    
    
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
        System.currentTimeMillis() - time
    
    fun checkTimeout(): Boolean {
        val isTimeout = time() >= period
        
        if(isTimeout) {
            timeout()
        }
        
        return isTimeout
    }
    
    fun update() {
        time = System.currentTimeMillis()
    }
    
    
    private fun startTimings() {
        time = System.currentTimeMillis()
    }
    
    private fun timeout() {
        time = 0
        clear()
    }
    
}