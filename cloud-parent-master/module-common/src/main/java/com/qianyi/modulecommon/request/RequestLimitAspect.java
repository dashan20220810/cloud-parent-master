package com.qianyi.modulecommon.request;

import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
public class RequestLimitAspect {

    private static ConcurrentHashMap<String, ExpiringMap<String, Integer>> book = new ConcurrentHashMap<>();


    @Pointcut("@annotation(requestLimit)")
    public void excudeService(RequestLimit requestLimit) {
    }

    @Around("excudeService(requestLimit)")
    public Object pointcut(ProceedingJoinPoint point, RequestLimit requestLimit) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        // 获取Map对象， 如果没有则返回默认值
        // 第一个参数是key， 第二个参数是默认值
        ExpiringMap<String, Integer> uc = book.getOrDefault(request.getRequestURI(), ExpiringMap.builder().variableExpiration().build());
        Integer count =  uc.getOrDefault(request.getRemoteAddr(), 0);

        if (count >= requestLimit.limit()) { // 超过次数，不执行目标方法
            return ResponseUtil.requestLimit();
        }
//第一次请求时，设置有效时间
        if (count == 0) {
            uc.put(request.getRemoteAddr(), count + 1, ExpirationPolicy.CREATED, requestLimit.timeout(), requestLimit.timeUnit());
        }else {
            uc.put(request.getRemoteAddr(), count + 1);
        }

        book.put(request.getRequestURI(), uc);

        // result的值就是被拦截方法的返回值
        return point.proceed();

    }
}
