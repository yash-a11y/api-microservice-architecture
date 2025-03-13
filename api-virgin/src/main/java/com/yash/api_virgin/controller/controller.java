package com.yash.api_virgin.controller;



import com.yash.api_virgin.service.centralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "plans/api/v.1")
public class controller {

    @Autowired
    private centralService centralService;

    @GetMapping("/virginplans")
    public  List<Map<String,String>> scrapePlans() throws InterruptedException{
        return centralService.getPrepaidPlans();
    }



//    @GetMapping(path = "/device")
//    ArrayList<device> device()
//    {
//        return deviceSrv.getDevice();
//    }




}
