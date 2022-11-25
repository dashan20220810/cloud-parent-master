package com.qianyi.livegoldenf.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class PublicGoldenFApiTest {

    @Test
    public void should_request_back(){
        PublicGoldenFApi publicGoldenFApi = new PublicGoldenFApi();
        PublicGoldenFApi.ResponseEntity responseEntity = publicGoldenFApi.getPlayerGameRecord(1643706855l,1644570855l,"PG",1,1000);
        log.info("{}",responseEntity);
    }
}