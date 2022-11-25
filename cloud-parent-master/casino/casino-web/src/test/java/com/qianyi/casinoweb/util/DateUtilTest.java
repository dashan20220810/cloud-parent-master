package com.qianyi.casinoweb.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class DateUtilTest {

    @Test
    public void should_convert_time(){
        String strTime = DateUtil.timeStamp2Date(1645418413950l,"");
        log.info("{}",strTime);
    }
}