package com.yash.api_rogers.controller;




import com.yash.api_rogers.models.Plan;
import com.yash.api_rogers.service.centralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "plans/api/v.1")
public class controller {




    @Autowired
    private centralService centralService;


    @GetMapping("/rogersplans")
    public List<Plan> getRogersPlans() {
        return centralService.getPrepaidPlans();
    }
    



//    @GetMapping(path = "/device")
//    ArrayList<device> device()
//    {
//        return deviceSrv.getDevice();
//    }




}
