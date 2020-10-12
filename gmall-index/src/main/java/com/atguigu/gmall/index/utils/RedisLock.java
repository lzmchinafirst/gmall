package com.atguigu.gmall.index.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;
    private Thread thread;

    //1.由于在这里我们采用的是微服务架构，为了避免由于其中某台服务器宕机所导致的死锁问题，
    //我们在这里应该设置redis锁的过期时间
    //2.由于业务的执行时间是不一致的，有可能导致业务运行时间超过redis锁的存活时间，这样当另外的线程获得锁
    //的时候有可能会导致误删，为了防止误删，我们应该设置每一个线程有其对应的具体的锁
    //3.为了保证解决并发问题，我们应该在加锁和解锁的时候考虑到原子性问题，在这里推荐使用lua脚本

    /**
     * 添加锁
     * @param lockKey
     * @param lockValue
     * @param aliveTime
     * @param timeUnit
     * @return
     */
    public boolean lock(String lockKey, String lockValue, Long aliveTime, TimeUnit timeUnit) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, aliveTime, timeUnit);
        //如果锁住了返回true，如果失败那么一直重试
        if(locked) {
            return true;
        } else {
            lock(lockKey,lockValue,aliveTime,timeUnit);
        }
        monitor(lockKey,lockValue,aliveTime);
        return true;
    }


    /**
     * 添加可重入锁
     * @param lockKey
     * @param lockValue
     * @param aliveTime
     * @return
     */
    public boolean lock(String lockKey,String lockValue, Long aliveTime) {
        String script = "if (redis.call('exists',KEYS[1])==0 or redis.call('hexists',KEYS[1],ARGV[1])==1) " +
                            "then redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                            "redis.call('expire',KEYS[1],ARGV[2]) " +
                            "return 1 " +
                        "else " +
                            "return 0 " +
                        "end";
        Boolean result = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockKey), lockValue, String.valueOf(aliveTime));
        if(!result) {
            try { Thread.sleep(20); } catch (Exception e) { e.printStackTrace(); }
            lock(lockKey,lockValue,aliveTime);
        }
        monitor(lockKey,lockValue,aliveTime);
        return true;
    }


    public void unLock(String lockKey, String lockValue) {
//        1.原始的实现锁的删除
//        if(StringUtils.equals(lockValue,redisTemplate.opsForValue().get(lockKey))) {
//            redisTemplate.delete(lockKey);
//        } else {
//            throw new RuntimeException("This lock is not belong to this thread, please check you redis cache alive time.");
//        }
//        2.第一种锁的版本，防止锁的误删
//        String script = "if(redis.call('get', KEYS[1])==ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
//        Boolean result = redisTemplate.execute(new DefaultRedisScript<>
//                (script, Boolean.class), Arrays.asList(lockKey), lockValue);
//        if(!result){
//            throw new RuntimeException("Sorry, this lock is not belong to you, please wait.");
//        }
//        3.第二种锁的版本，可重入锁的删除

        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==0) then return nil " +
                            "elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1) > 0) then return 0 " +
                            "else redis.call('del',KEYS[1]) return 1 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockKey), lockValue);
        thread.interrupt();
        if(result == null) {
            throw new RuntimeException("Sorry, this lock is not exist or belong to you, please wait.");
        }
    }

    /**
     * 设置监听器，为了防止锁在任务没有执行完的时候就释放，我们单独创建一个线程给它进行自动续期
     */
    private void monitor(String lockKey, String lockValue, Long lockTime) {
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==1) then redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";
        thread = new Thread(() -> {
            while (true) {
                try { Thread.sleep(lockTime * 500); } catch (Exception e) { e.printStackTrace(); }
                this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockKey),lockValue,String.valueOf(lockTime));
            }
        });
        thread.start();
    }
}
