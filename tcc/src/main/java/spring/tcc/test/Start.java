package spring.tcc.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import spring.tcc.service.TccServiceImpl;
import spring.tcc.service.TccServiceUtil;

@ComponentScan("spring.tcc")
@ImportResource("classpath:/consumer.xml")
public class Start {

	public static void main(String[] args) {
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Start.class);
		
		Map<String, Object> params = new HashMap();
		params.put("orderNo", new Date().getTime()+"");
		params.put("userName", "zhangsan");
		params.put("comName", "zibang");
		params.put("amount", 1000.00d);
		int x = 1;
		while(x--> 0){
			try {
			Thread.sleep(200);
			params.put("orderNo", new Date().getTime()+"");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			TccServiceUtil.executeService(TccServiceImpl.class, params);
		}
		
	}

}
