package com.qianyi.casinoadmin.service;

import com.qianyi.casinoadmin.model.SysUserLoginLog;
import com.qianyi.casinoadmin.repository.SysUserLoginLogRepository;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;

@Service
@Transactional
public class SysUserLoginLogService {

    @Autowired
    SysUserLoginLogRepository sysUserLoginLogRepository;

    /**
     * 独立线程执行此方法
     * @param sysUserLoginLog
     */
    @Async
    public void saveSyncLog(SysUserLoginLog sysUserLoginLog) {
        if (!ObjectUtils.isEmpty(sysUserLoginLog.getIp())) {
            //得到IP归属地
            String address = IpUtil.getAddress(sysUserLoginLog.getIp());
            if (address != null) {
                sysUserLoginLog.setAddress(address);
            }
        }

        sysUserLoginLogRepository.save(sysUserLoginLog);
    }
}
