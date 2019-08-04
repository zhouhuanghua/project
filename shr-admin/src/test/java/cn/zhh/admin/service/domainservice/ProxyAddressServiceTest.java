package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.entity.ProxyAddress;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.enums.IsDeletedEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ProxyAddressServiceTest {

    @Autowired
    private ProxyAddressService proxyAddressService;

    @Test
    public void testInsert() {
        ProxyAddress proxyAddress = new ProxyAddress();
        proxyAddress.setIp("127.0.0.1");
        proxyAddress.setPort("8080");
        proxyAddress.setType("HTTP代理");
        proxyAddress.setCreator(SysConsts.DEFAULT_USER_NAME);
        proxyAddress.setCreateTime(new Date());
        proxyAddress.setIsDeleted(IsDeletedEnum.NO.getCode());

        proxyAddressService.insert(proxyAddress);

        Assert.assertNotNull(proxyAddress.getId());
    }
}