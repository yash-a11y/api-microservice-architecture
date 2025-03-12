package com.yash.api_koodo.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yash.api_koodo.model.KoodoPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class centralService {

    @Autowired
    private redisService redisService;

    @Autowired
    private KoodoScraperService koodoScraperService;

    public List<KoodoPlan> getPrepaidPlans()
    {
        List<KoodoPlan> koodoPlans = redisService.getValue("koodo", new TypeReference<List<KoodoPlan>>() {
        });


        if(koodoPlans != null)
        {
            return koodoPlans;
        }
        else{

            List<KoodoPlan> planList = koodoScraperService.scrapePrepaidPlans();
            redisService.set("koodo", planList, 300l);
            return planList;

        }
    }


}
