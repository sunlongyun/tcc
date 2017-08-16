#tcc分布式事务介绍
关于TCC（Try-Confirm-Cancel）的概念，最早是由Pat Helland于2007年发表的一篇名为《Life beyond Distributed Transactions:an Apostate’s Opinion》的论文提出。在该论文中，TCC还是以Tentative-Confirmation-Cancellation作为名称；正式以Try-Confirm-Cancel作为名称的，可能是Atomikos（Gregor Hohpe所著书籍《Enterprise Integration Patterns》中收录了关于TCC的介绍，提到了Atomikos的Try-Confirm-Cancel，并认为二者是相似的概念）。
国内最早关于TCC的报道，应该是InfoQ上对阿里程立博士的一篇采访。经过程博士的这一次传道之后，TCC在国内逐渐被大家广为了解并接受。相应的实现方案和开源框架也先后被发布出来，ByteTCC就是其中之一。
TCC事务机制相对于传统事务机制（X/Open XA），其特征在于它不依赖资源管理器(RM)对XA的支持，而是通过对（由业务系统提供的）业务逻辑的调度来实现分布式事务。对于业务系统中一个特定的业务逻辑S，其对外提供服务时，必须接受一些不确定性，即对业务逻辑执行的一次调用仅是一个临时性操作，调用它的消费方服务M保留了后续的取消权。如果M认为全局事务应该rollback，它会要求取消之前的临时性操作，这就对应S的一个取消操作。而当M认为全局事务应该commit时，它会放弃之前临时性操作的取消权，这对应S的一个确认操作。 每一个初步操作，最终都会被确认或取消。因此，针对一个具体的业务服务，TCC事务机制需要业务系统提供三段业务逻辑：初步操作Try、确认操作Confirm、取消操作Cancel。
1. 初步操作（Try）
TCC事务机制中的业务逻辑（Try），从执行阶段来看，与传统事务机制中业务逻辑相同。但从业务角度来看，是不一样的。TCC机制中的Try仅是一个初步操作，它和后续的次确认一起才能真正构成一个完整的业务逻辑。因此，可以认为[传统事务机制]的业务逻辑 = [TCC事务机制]的初步操作（Try） + [TCC事务机制]的确认逻辑（Confirm）。TCC机制将传统事务机制中的业务逻辑一分为二，拆分后保留的部分即为初步操作（Try）；而分离出的部分即为确认操作（Confirm），被延迟到事务提交阶段执行。
TCC事务机制以初步操作（Try）为中心，确认操作（Confirm）和取消操作（Cancel）都是围绕初步操作（Try）而展开。因此，Try阶段中的操作，其保障性是最好的，即使失败，仍然有取消操作（Cancel）可以将其不良影响进行回撤。
2. 确认操作（Confirm）
确认操作（Confirm）是对初步操作（Try）的一个补充。当TCC事务管理器认为全局事务可以正确提交时，就会逐个执行初步操作（Try）指定的确认操作（Confirm），将初步操作（Try）未完成的事项最终完成。
3. 取消操作（Cancel）
取消操作（Cancel）是对初步操作（Try）的一个回撤。当TCC事务管理器认为全局事务不能正确提交时，就会逐个执行初步操作（Try）指定的取消操作（Cancel），将初步操作（Try）已完成的事项全部撤回。
在传统事务机制中，业务逻辑的执行和事务的处理，是在不同的阶段由不同的部件来处理的：业务逻辑部分访问资源实现数据存储，其处理是由业务系统负责；事务处理部分通过协调资源管理器以实现事务管理，其处理由事务管理器来负责。二者没有太多交互的地方，所以，传统事务管理器的事务处理逻辑，仅需要着眼于事务完成（commit/rollback）阶段，而不必关注业务执行阶段。而在TCC事务机制中的业务逻辑和事务处理，其关系就错综复杂：业务逻辑（Try/Confirm/Cancel）阶段涉及所参与资源事务的commit/rollback；全局事务commit/rollback时又涉及到业务逻辑（Try/Confirm/Cancel）的执行。


#该tcc分布式事务框架特点
1.支持分布式环境下的负载均衡，减少服务器压力
2.有效解决单点故障。某消费者获得service任务后，突然宕机。其他线程可以继续执行该任务，不会出现任务丢失或者任务执行一部分的状态。
3.业务逻辑解耦，用户只需要关注业务逻辑（实现tcc接口）。该插件保证业务的事务一致性。



#该tcc 分布式事务框架使用步骤
1.tcc-util-0.0.1.jar加入自己资源库
2.pom 引用
    <dependency>
	   <groupId>tcc</groupId>
	  <artifactId>tcc-util</artifactId>
	  <version>0.0.1-SNAPSHOT</version>
   </dependency>
   
   
3.自定义service实现接口tcc。例如：
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
     
  4. 创建tcc工具类的bean，需要注入属性jedisConnectionFactory
  
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
   
   5.使用分布式事务
   TccServiceUtil.executeService(TccServiceImpl.class, params);
   
