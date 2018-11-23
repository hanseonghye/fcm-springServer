package com.example.demo;

import com.example.demo.server.CcsClient;
import com.example.demo.util.Util;
import org.jivesoftware.smack.XMPPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class DemoApplication {

//    @Autowired
//    private CcsClient ccsClient;
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        CcsClient ccsClient = CcsClient.prepareClient(Util.fcmProjectSenderId, Util.fcmServerKey, true);

        try {
            ccsClient.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
        }

    }

//    @PostConstruct
//    public void init() {
//        System.out.println("working!!!!!!@!@!@!@!@!@!!@!@!@!");
//
//        ccsClient = CcsClient.prepareClient(Util.fcmProjectSenderId, Util.fcmServerKey, true);
//
//        try {
//            ccsClient.connect();
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @PreDestroy
//    public void destroy() {
//        System.out.println("끄아아아아아아아아아ㅏ아아");
//    }

}
