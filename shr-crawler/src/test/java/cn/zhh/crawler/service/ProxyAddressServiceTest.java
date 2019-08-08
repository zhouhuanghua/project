package cn.zhh.crawler.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ProxyAddressServiceTest {

    @Autowired
    private ProxyAddressService proxyAddressService;

    @Test
    public void cralw() throws Exception {
        proxyAddressService.cralw(1);
    }

    @Test
    public void randomProxyAddress() throws Exception {
    }

}