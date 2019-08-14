package cn.zhh.admin.service.mq;

import cn.zhh.admin.entity.SchoolInternshipCompany;
import cn.zhh.admin.entity.SchoolInternshipJob;
import cn.zhh.admin.service.db.SchoolInternshipCompanyService;
import cn.zhh.admin.service.db.SchoolInternshipJobService;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.SchoolInternshipMsg;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 校招-实习消息服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class SchoolInternshipMsgServiceImpl implements SchoolInternshipMsgService {

    @Autowired
    private SchoolInternshipCompanyService companyService;

    @Autowired
    private SchoolInternshipJobService jobService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(SchoolInternshipMsg msg) {
        // 插入公司记录
        SchoolInternshipCompany company = new SchoolInternshipCompany();
        BeanUtils.copyProperties(msg, company);
        company.setCreator(SysConsts.DEFAULT_USER_NAME);
        company.setCreateTime(new Date());
        company.setIsDeleted(IsDeletedEnum.NO.getCode());
        companyService.insert(company);

        // 插入岗位记录
        List<SchoolInternshipMsg.JobMsg> jobMsgList = msg.getJobInfo();
        for (SchoolInternshipMsg.JobMsg jobMsg : jobMsgList) {
            SchoolInternshipJob job = new SchoolInternshipJob();
            // 拷贝属性
            BeanUtils.copySelectedProperties(jobMsg, job,
                    "jobLink:url",
                    "jobName:name",
                    "jobUniqueKey:uniqueKey",
                    "jobWorkPlace:workPlace",
                    "jobDeadline:deadline",
                    "jobIntroduce:introduce",
                    "jobDescription:description");
            job.setCreator(SysConsts.DEFAULT_USER_NAME);
            job.setCreateTime(new Date());
            job.setIsDeleted(IsDeletedEnum.NO.getCode());
            job.setCompanyId(company.getId());
            jobService.insert(job);
        }
    }
}
