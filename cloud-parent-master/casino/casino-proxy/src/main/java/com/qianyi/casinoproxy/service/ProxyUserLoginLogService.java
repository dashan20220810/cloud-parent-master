package com.qianyi.casinoproxy.service;

import com.qianyi.casinoproxy.model.ProxyUserLoginLog;
import com.qianyi.casinoproxy.repository.ProxyUserLoginLogRepository;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;

@Service
@Transactional
public class ProxyUserLoginLogService {

    @Autowired
    private ProxyUserLoginLogRepository proxyUserLoginLogRepository;

    /**
     * 独立线程执行此方法
     * @param proxyUserLoginLog
     */
    @Async
    public void saveSyncLog(ProxyUserLoginLog proxyUserLoginLog) {
        if (!ObjectUtils.isEmpty(proxyUserLoginLog.getIp())) {
            //得到IP归属地
            String address = IpUtil.getAddress(proxyUserLoginLog.getIp());
            if (address != null) {
                proxyUserLoginLog.setAddress(address);
            }
        }

        proxyUserLoginLogRepository.save(proxyUserLoginLog);
    }
}
