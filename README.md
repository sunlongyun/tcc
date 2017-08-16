# tcc 分布式事务使用步骤


1.tcc-util-0.0.1.jar加入自己资源库


2.自定义service实现接口tcc。例如：
    @Service
    public class TccServiceImpl implements Tcc{
        @Override
      public void tryAction(Map<String, Object> params) throws Exception {
          //TODO 写自己的预处理程序
      }
      @Override
      public void commitAction(Map<String, Object> params) throws Exception {
       //TODO 写自己的确认提交程序
      }
      @Override
     public void rollbackAction(Map<String, Object> params) throws Exception {
      //TODO 写自己的回滚程序
     }
     }
     
  3. 创建tcc工具类的bean，需要注入属性jedisConnectionFactory
  
  例如：
  
  <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig"> 
        <property name="maxIdle" value="50" /> 
        <property name="maxTotal" value="10" /> 
        <property name="blockWhenExhausted" value="true" /> 
        <property name="maxWaitMillis" value="1000" /> 
        <property name="testOnBorrow" value="true" />  
    </bean> 
    <bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory"> 
        <property name="hostName" value="master" /> 
        <property name="port" value="6379"/> 
        <property name="poolConfig" ref="jedisPoolConfig" /> 
        <property name="usePool" value="true"/> 
        <property name="password" value="123456"/>
    </bean> 
    
   <bean class="spring.tcc.service.TccServiceUtil">
   	<property name="jedisConnectionFactory" ref="jedisConnectionFactory"></property>
   </bean>
   
   4.使用分布式事务
   TccServiceUtil.executeService(TccServiceImpl.class, params);
   
  
