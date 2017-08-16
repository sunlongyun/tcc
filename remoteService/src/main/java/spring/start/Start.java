package spring.start;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * Hello world!
 *
 */
@ComponentScan("spring")
@ImportResource("classpath:/provider.xml")
public class Start 
{
    public static void main( String[] args )
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Start.class);
        System.out.println("启动成功-------------------");
        while(true){
        	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}
