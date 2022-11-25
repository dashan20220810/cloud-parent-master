package com.qianyi.casinoadmin.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ThridUserBalanceSumService {


    @Autowired
    private RedisUtil redisUtil;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_10);

    @Autowired
    private UserMoneyService userMoneyService;

    public void setRedisSABAMoneyTotal(List<UserThird> sabaThird) {
        if (LoginUtil.checkNull(sabaThird) || sabaThird.size() == CommonConst.NUMBER_0){
            return ;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(sabaThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:sabaThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshSABAUserId(u.getUserId().toString());

                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        //存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_SABASPORT;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    public void setRedisOBTYMoneyTotal(List<UserThird> obtyThird) {
        if (LoginUtil.checkNull(obtyThird) || obtyThird.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obtyThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:obtyThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOBTY(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBTY;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    public void setRedisOBDJMoneyTotal(List<UserThird> obdjThird) {
        if (LoginUtil.checkNull(obdjThird) || obdjThird.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(obdjThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:obdjThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOB(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBDJ;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }

    @Async
    public void setRedisPGMoneyTotal(List<UserThird> pgCq9Third) {
        if (LoginUtil.checkNull(pgCq9Third) || pgCq9Third.size() == CommonConst.NUMBER_0){
            return;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(pgCq9Third.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:pgCq9Third){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshPGAndCQ9UserId(u.getUserId().toString());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            synchronized (this){
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_PG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }


    public void setRedisWMMoneyTotal(List<UserThird> wmThird){
        if (LoginUtil.checkNull(wmThird) || wmThird.size() == CommonConst.NUMBER_0){
            return ;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(wmThird.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:wmThird){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.getWMonetUser(u);

                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        //存入缓存
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_WM_BIG;
        redisUtil.set(key, sum, Constants.THIRD_BALANCE_TTL);
    }
}
