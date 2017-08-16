package spring.beans;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceUtil implements ApplicationContextAware{
	private static ApplicationContext context = null;
	private static ThreadLocal<Connection> local = new ThreadLocal<>();
	/**
	 * 异常测试
	 */
	public  static void testException(){
		int num = 4;
		if(num >= 3 && num <=5){
			throw new RuntimeException("异常测试");
		}
	}
	/**
	 * 异常随机测试
	 */
	public  static void testRandomException(){
		int num = (int)(Math.random() * 15);
		if(num >= 3 && num <=5){
			throw new RuntimeException("异常测试");
		}
	}
	public static void removeCon(){
		Connection con =  local.get();
		if(null != con){
			try {
				if(!con.isClosed()){
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			local.remove();
		}
	}
	@Bean("userDataSource")
	public DataSource getUserDataSource(){
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:mysql://n1:3306/user_account");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		dataSource.setMaxIdle(255);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		return dataSource;
	}
	
	@Bean("comDataSource")
	public DataSource getComDataSource(){
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:mysql://n2:3306/com_account");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		dataSource.setMaxIdle(255);
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		return dataSource;
	}
	/**
	 * 获得数据库连接
	 * @param dataSourceName
	 * @return
	 */
	public static Connection getCon(String dataSourceName){
		if(null == local.get()){
			DataSource dataSource = (DataSource) context.getBean(dataSourceName);
			try {
				Connection con = dataSource.getConnection();
				local.set(con);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return local.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
		
	}
}
