package spring.tcc.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import spring.tcc.beans.OrderItem;
@Service
public class TccServiceUtil implements ApplicationContextAware{
	private  static ApplicationContext context = null;
	private static int size = 3;
	/**
	 * 执行try任务的线程池
	 */
	private static ExecutorService tryPool = Executors.newFixedThreadPool(size);
	/**
	 * 执行commit任务的线程池
	 */
	private static ExecutorService commitPool = Executors.newFixedThreadPool(size);
	/**
	 * 执行cancel任务的线程池
	 */
	private static ExecutorService canelPool = Executors.newFixedThreadPool(size);
	/**
	 * 即将执行try的订单列表
	 */
	private static String NEED_TRY_LIST = "need_try_list";
	/**
	 * 即将处理commit的订单列表
	 */
	private static String NEED_COMMIT_LIST = "need_commit_list";
	/**
	 * 即将执行cancel的订单列表
	 */
	private static String NEED_CANCEL_LIST = "need_cancel_list";
	/**
	 * 无法处理的订单信息
	 */
	private static String ERROR_CANCEL_LIST = "error_cancel_list";
	private static int TIMES = 15;
	private static int LOCK_TIMES = TIMES * 100;
	 /**
	  * 
	  * 作者：孙龙云
	  * 时间：2017年8月10日 下午4:09:05
	  * @param className 上下文中存才的bean的类型
	  * @param params try，commit，cancel函数需要的参数
	  */
	 public static void executeService(Class<? extends Tcc> className, Map<String, Object> params){
		 Object target = context.getBean(className);
		 if(null == target){
			 throw new RuntimeException("在上下文中找不到类型为"+className.getName()+"的bean!");
		 }
		 Jedis jedis = getJedis();
		 long r = jedis.lpush(NEED_TRY_LIST.getBytes(), ObjectToByte(new OrderItem(params, className)));
		 System.out.println("添加任务结果:"+r);
	 }
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		 
		TccServiceUtil.context = context;
		final ApplicationContext ctx = context;
		/**
		 * 开启线程执行任务
		 */
		for(int i=0; i< size; i++){
			tryPool.submit(new Runnable() {
				Jedis jedis = getJedis();
				@Override
				public void run() {
					while(true){
						
						byte[] bytes =  jedis.brpoplpush(NEED_TRY_LIST.getBytes(), NEED_TRY_LIST.getBytes(), TIMES * 20);
						if(null == bytes){
							continue;
						}
						
						OrderItem orderItem = (OrderItem) ByteToObject(bytes);
						String orderNo = (String) orderItem.getParams().get("orderNo");
						String nodeKey = "orderNo:"+orderNo;
						//获得锁
						long r = jedis.setnx(nodeKey.getBytes(), "".getBytes());
						System.out.println("r="+r);
						if(r > 0){
							jedis.expire(nodeKey.getBytes(),  LOCK_TIMES);
						}else{
							try {
								Thread.sleep(TIMES*1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						Tcc tcc = ctx.getBean(orderItem.getClassName());
						
						/**
						 * 最多尝试三次
						 */
						int times = 3;
						while(true){
							try {
								tcc.tryAction(orderItem.getParams());
								
								/**
								 * 避免重复添加
								 */
								try{
									jedis.lrem(NEED_COMMIT_LIST.getBytes(), 0, ObjectToByte(orderItem));
								}catch (Exception e) {
									e.printStackTrace();
								}
								jedis.lpush(NEED_COMMIT_LIST.getBytes(), ObjectToByte(orderItem));
								jedis.lrem(NEED_TRY_LIST.getBytes(), 0, bytes);
								
							} catch (Exception e) {
								System.out.println(this.getClass().getName()+"----------"+e.getMessage());
								if(-- times > 0){
									try {
										Thread.sleep(500);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									continue;
								}
								/**
								 * 避免重复添加
								 */
								try{
									jedis.lrem(NEED_CANCEL_LIST.getBytes(), 0, bytes);
								}catch (Exception e1) {
									e1.printStackTrace();
								}
								
								jedis.lpush(NEED_CANCEL_LIST.getBytes(), bytes);
								jedis.lrem(NEED_TRY_LIST.getBytes(), 0, bytes);
							}
							
							break;
						}
						//释放锁
						jedis.del(nodeKey.getBytes());
					}
					
				}
			});
			
			
			commitPool.submit(new Runnable() {
				Jedis jedis = getJedis();
				@Override
				public void run() {
					
					while(true){
						byte[] bytes =  jedis.brpoplpush(NEED_COMMIT_LIST.getBytes(), NEED_COMMIT_LIST.getBytes(), TIMES * 20);
						if(null == bytes){
							continue;
						}
						OrderItem orderItem = (OrderItem) ByteToObject(bytes);
						String orderNo = (String) orderItem.getParams().get("orderNo");
						String nodeKey = "orderNo:"+orderNo;
						//获得锁
						long r = jedis.setnx(nodeKey.getBytes(), "".getBytes());
						System.out.println("r="+r);
						if(r > 0){
							jedis.expire(nodeKey.getBytes(),  TIMES);
						}else{
							try {
								Thread.sleep(TIMES*1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						Tcc tcc = ctx.getBean(orderItem.getClassName());
						
						/**
						 * 最多尝试三次
						 */
						int times = 3;
						while(true){
							try {
								tcc.commitAction(orderItem.getParams());
								jedis.lrem(NEED_COMMIT_LIST.getBytes(), 0, bytes);
							} catch (Exception e) {
								System.out.println(this.getClass().getName()+"----------"+e.getMessage());
								if(-- times > 0){
									try {
										Thread.sleep(500);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									continue;
								}
								/**
								 * 避免重复添加
								 */
								try{
									jedis.lrem(NEED_CANCEL_LIST.getBytes(), 0, bytes);
								}catch (Exception e1) {
									e1.printStackTrace();
								}
								
								jedis.lpush(NEED_CANCEL_LIST.getBytes(), bytes);
								jedis.lrem(NEED_COMMIT_LIST.getBytes(), 0, bytes);
							}
							
							break;
						}
						//释放锁
						jedis.del(nodeKey.getBytes());
					}
				}
			});
			
			
			canelPool.submit(new Runnable() {
				Jedis jedis = getJedis();
				@Override
				public void run() {
					while(true){
						byte[] bytes =  jedis.brpoplpush(NEED_CANCEL_LIST.getBytes(), NEED_CANCEL_LIST.getBytes(), TIMES * 20);
						if(null == bytes){
							continue;
						}
						OrderItem orderItem = (OrderItem) ByteToObject(bytes);
						String orderNo = (String) orderItem.getParams().get("orderNo");
						String nodeKey = "orderNo:"+orderNo;
						//获得锁
						long r = jedis.setnx(nodeKey.getBytes(), "".getBytes());
						System.out.println("r="+r);
						if(r > 0){
							jedis.expire(nodeKey.getBytes(),  TIMES);
						}else{
							try {
								Thread.sleep(TIMES*1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						Tcc tcc = ctx.getBean(orderItem.getClassName());
						
						/**
						 * 最多尝试三次
						 */
						int times = 6;
						while(true){
							try {
								tcc.rollbackAction(orderItem.getParams());
								jedis.lrem(NEED_CANCEL_LIST.getBytes(), 0, bytes);
							} catch (Exception e) {
								System.out.println(this.getClass().getName()+"----------"+e.getMessage());
								if(-- times > 0){
									try {
										Thread.sleep(TIMES*1000);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									continue;
								}
								/**
								 *避免重复添加
								 */
								try {
									jedis.lrem(ERROR_CANCEL_LIST.getBytes(), 0, bytes);
								} catch (Exception e2) {
									e2.printStackTrace();
								}
								jedis.lpush(ERROR_CANCEL_LIST.getBytes(), bytes);
								jedis.lrem(NEED_CANCEL_LIST.getBytes(), 0, bytes);
							}
							break;
							
						}
						//释放锁
						jedis.del(nodeKey.getBytes());
					}
				}
			});
		}
	}
	/**
	 * 字节转对象
	 * 作者：孙龙云
	 * 时间：2017年8月9日 上午9:23:10
	 * @param bytes
	 * @return
	 */
	public static Object ByteToObject(byte[] bytes) {  
		Object obj = null;  
		try {  
		    ByteArrayInputStream bi = new ByteArrayInputStream(bytes);  
		    ObjectInputStream oi = new ObjectInputStream(bi);  
		    obj = oi.readObject();  
		    bi.close();  
		    oi.close();  
		} catch (Exception e) {  
		    System.out.println("translation" + e.getMessage());  
		    e.printStackTrace();  
		}  
		 return obj;  
	   } 
 
	 /**
     * 对象转Byte数组
     * @param obj
     * @return
     */
    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                   e.printStackTrace();
                }
            }

        }
        return bytes;
    }
    private static Jedis getJedis(){
    	Jedis  jedis = new Jedis("master", 6379);
        //权限认证
        jedis.auth("123456"); 
        return jedis;
    }
}
