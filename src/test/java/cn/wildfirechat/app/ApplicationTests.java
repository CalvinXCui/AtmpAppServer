package cn.wildfirechat.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
	Service service = new ServiceImpl();
	@Test
	public void contextLoads() {
		RestResult byMobile = service.findByMobile("18791532019");
		System.out.println(byMobile);
	}



}
