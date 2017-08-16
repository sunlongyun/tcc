package spring.tcc.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.Jedis;

@Configuration
@PropertySource("classpath:/config.properties")
public class RedisTemplateConfiguration {
	/**
	 * 主机
	 */
	@Value("${hostName}")
	private String hostName;
	/**
	 * 端口
	 */
	@Value("${port}")
	private Integer port;
	/**
	 * 密码
	 */
	@Value("${password}")
	private String password;
	/**
	 * 数据库
	 */
	@Value("${dataBase}")
	private Integer dataBase;
	/**
	 * 获取redisTemplate
	 * 作者：孙龙云
	 * 时间：2017年8月8日 下午3:31:28
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> getRedisTemplate(){
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setHostName(hostName);
		jedisConnectionFactory.setPort(port);
		jedisConnectionFactory.setPassword(password);
		jedisConnectionFactory.setDatabase(dataBase);
		
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		
		return redisTemplate;
	}
	@Bean
	@Scope("prototype")
	public Jedis getJedis(){
		Jedis jedis = null;
		jedis = new Jedis(hostName, port);
		jedis.auth(password);
		return jedis;
	}
	
}
