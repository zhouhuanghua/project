package cn.zhh.admin.service.es;

import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PositionSearchServiceTest {

    @Autowired
    private PositionSearchService positionSearchService;

    @Test
    public void insert() throws Exception {
    }

    @Test
    public void pageQueryByCondition() throws Exception {
        PositionSearchReq req = new PositionSearchReq();
        req.setContent("玉兔时代");
        req.setPageNum(0);
        req.setPageSize(10);
        Page<PositionSearchVO> page = positionSearchService.pageQueryByCondition(req);
        System.out.println(page);
    }

    @Test
    public void deleteById() {
        positionSearchService.deleteById(null);
    }

}