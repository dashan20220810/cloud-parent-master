package com.qianyi.casinoreport.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShareProfitUtilsTest {

    @Test
    public void should_process_correct(){
        boolean result = ShareProfitUtils.compareIntegerNotNull(1l);
        System.out.println(result);
    }
}