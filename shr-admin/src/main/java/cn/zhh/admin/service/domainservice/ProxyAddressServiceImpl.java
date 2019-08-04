package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.dao.db.ProxyAddressDao;
import cn.zhh.admin.entity.ProxyAddress;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.enums.IsDeletedEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class ProxyAddressServiceImpl implements ProxyAddressService {

    @Autowired
    private ProxyAddressDao dao;

    @Override
    public JpaRepository<ProxyAddress, Long> dao() {
        return dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProxyAddress insert(ProxyAddress proxyAddress) {
        // 判断是否存在相同的IP和端口，有就记录日志并忽略，没有就插入数据
        Example<ProxyAddress> proxyAddressExample = buildExample(ProxyAddress.class,
                "ip", proxyAddress.getIp(), "port", proxyAddress.getPort());
        if (exists(proxyAddressExample)) {
            // 如果已经存在，就只记录日志
            log.info("代理地址已存在，自动忽略。IP：{}，端口：{}", proxyAddress.getIp(), proxyAddress.getPort());
        } else {
            // 如果不存在，插入数据
            proxyAddress.setCreator(SysConsts.DEFAULT_USER_NAME);
            proxyAddress.setCreateTime(new Date());
            proxyAddress.setIsDeleted(IsDeletedEnum.NO.getCode());
            save(proxyAddress);
        }
        return proxyAddress;
    }
}
