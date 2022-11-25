package com.qianyi.payadmin.filter;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XssFilter extends com.qianyi.modulecommon.filter.XssFilter {
    @Override
    public void setPassList(List<String> passList) {
        passList.add("/error");
    }
}
