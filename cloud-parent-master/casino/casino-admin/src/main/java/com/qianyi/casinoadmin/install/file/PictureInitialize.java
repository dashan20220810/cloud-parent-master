package com.qianyi.casinoadmin.install.file;

import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PictureInitialize {
    public static Map<String, String> bank = new HashMap<>();
    static {
        bank.put("/img/bank-photo-gongshang.png","工商银行");
        bank.put("/img/bank-photo-guangda.png","广大银行");
        bank.put("/img/bank-photo-guangdongfazhan.png","广东发展银行");
        bank.put("/img/bank-photo-jianshen.png","建设银行");
        bank.put("/img/bank-photo-jiaotong.png","交通银行");
        bank.put("/img/bank-photo-minsheng.png","民生银行");
        bank.put("/img/bank-photo-nongye.png","农业银行");
        bank.put("/img/bank-photo-pufa.png","上海浦东发展银行");
        bank.put("/img/bank-photo-xingye.png","兴业银行");
        bank.put("/img/bank-photo-youzheng.png","邮政银行");
        bank.put("/img/bank-photo-zhaoshang.png","招商银行");
        bank.put("/img/bank-photo-zhongguo.png","中国银行");
        bank.put("/img/bank-photo-zhongxin.png","中信银行");
    }


    public static List<CustomerConfigure> customer = new ArrayList<>();
    static {
        CustomerConfigure qq=new CustomerConfigure();
        qq.setCustomerMark(1);
        qq.setCustomer("qq号");
        qq.setState(Constants.close);
        qq.setAppIconUrl("/customer/app_QQ@2x.png");
        qq.setPcIconUrl("/customer/qq82.png");
        customer.add(qq);


        CustomerConfigure skype=new CustomerConfigure();
        skype.setCustomerMark(2);
        skype.setCustomer("skype号");
        skype.setState(Constants.close);
        skype.setAppIconUrl("/customer/app_skype@2x.png");
        skype.setPcIconUrl("/customer/skype.png");
        customer.add(skype);


        CustomerConfigure tg=new CustomerConfigure();
        tg.setCustomerMark(3);
        tg.setCustomer("telegram号");
        tg.setState(Constants.close);
        tg.setAppIconUrl("/customer/app_telegram@2x.png");
        tg.setPcIconUrl("/customer/tg.png");
        customer.add(tg);

        CustomerConfigure whats=new CustomerConfigure();
        whats.setCustomerMark(4);
        whats.setCustomer("whatsApp号");
        whats.setState(Constants.close);
        whats.setAppIconUrl("/customer/app_kefu_whatsapp@2x.png");
        whats.setPcIconUrl("/customer/whatsApp.png");
        customer.add(whats);

        CustomerConfigure phone=new CustomerConfigure();
        phone.setCustomerMark(5);
        phone.setCustomer("手机号码");
        phone.setCustomerAccount("88888888");
        phone.setState(Constants.open);
        phone.setAppIconUrl("/customer/app_kefu_shoujihaoma@2x.png");
        phone.setPcIconUrl("/customer/pc_shoujihaoma.png");
        customer.add(phone);


        CustomerConfigure onlineUrl=new CustomerConfigure();
        onlineUrl.setCustomerMark(6);
        onlineUrl.setCustomerAccount("http://www.kk1688.net/#/test");
        onlineUrl.setCustomer("在线客服");
        onlineUrl.setState(Constants.open);
        onlineUrl.setAppIconUrl("/customer/app_zaixiankefu@2x.png");
        onlineUrl.setPcIconUrl("/customer/pc_zaixiankefu.png");
        customer.add(onlineUrl);

        CustomerConfigure wx=new CustomerConfigure();
        wx.setCustomerMark(7);
        wx.setCustomer("微信号");
        wx.setState(Constants.close);
        wx.setAppIconUrl("/customer/app_wx@2x.png");
        wx.setPcIconUrl("/customer/wx.png");
        customer.add(wx);
    }



    public static Map<String, Integer> PCbanner = new HashMap<>();
    static {
        PCbanner.put("/banner/6.jpg", CommonConst.NUMBER_6);
        PCbanner.put("/banner/7.jpg",CommonConst.NUMBER_7);
        PCbanner.put("/banner/8.jpg",CommonConst.NUMBER_8);
    }
    public static Map<String, Integer> APPbanner = new HashMap<>();
    static {
        APPbanner.put("/banner/1.png",CommonConst.NUMBER_1);
        APPbanner.put("/banner/2.png",CommonConst.NUMBER_2);
        APPbanner.put("/banner/3.png",CommonConst.NUMBER_3);
    }
    public void single(){
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("img/*.*");
            System.out.println(resources.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public MultipartFile single(String fileNameAndPath){
        Resource resource = new ClassPathResource(fileNameAndPath);
        String fileName = resource.getFilename();
        String fileNameNoExtension = getFileNameNoExtension(fileName);
        InputStream inputStream = null;
        MultipartFile multipartFile = null;
        try {
            inputStream = resource.getInputStream();
            multipartFile =  new MockMultipartFile(fileNameNoExtension, fileName, "text/plain", inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return multipartFile;
    }

    public  String getFileNameNoExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
