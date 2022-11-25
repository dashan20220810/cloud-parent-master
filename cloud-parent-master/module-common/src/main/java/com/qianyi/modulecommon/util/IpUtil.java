package com.qianyi.modulecommon.util;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class IpUtil {

    /**
     * 获取客户机的ip地址
     *
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
//        log.info("ip截取前={}",ip);
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
//        log.info("ip截取后={}",ip);
        return ip;
    }

    public static String getAddress(String ip) {
        try {
            DbConfig config = new DbConfig();

            String dbPath = "";
            String filename = "/ip2region.db";
            Boolean isWindows = CommonUtil.isWindows();
            if (isWindows) {
                dbPath = IpUtil.class.getResource(filename).getPath();
            } else {
                dbPath = "/usr/java/config" + filename;
            }

            DbSearcher searcher = new DbSearcher(config, dbPath);
            DataBlock block = searcher.btreeSearch(ip);
            return block.getRegion();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String ip = "13.230.72.205";
        String address = IpUtil.getAddress(ip);
        System.out.println("IP:" + address);
    }
}
