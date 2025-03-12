package com.yash.api_koodo.controller;



import com.yash.api_koodo.model.KoodoPlan;
import com.yash.api_koodo.services.KoodoScraperService;
import com.yash.api_koodo.services.centralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "plans/api/v.1")
public class controller {



    @Autowired
    private
    KoodoScraperService koodoScraperService;

    @Autowired
    private centralService centralService;


    @GetMapping("/koodoplans")
    public List<KoodoPlan> getKoodoPlans() {
        return centralService.getPrepaidPlans();
    }
    



//    @GetMapping(path = "/device")
//    ArrayList<device> device()
//    {
//        return deviceSrv.getDevice();
//    }




}
