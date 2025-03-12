package com.yash.api_rogers.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yash.api_rogers.models.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class centralService
{

    @Autowired
    private redisService redisService;

    @Autowired
    private WebScraperService webScraperService;

    public List<Plan> getPrepaidPlans()
    {
        List<Plan> rogersPlan = redisService.getValue("rogers", new TypeReference<List<Plan>>() {
        });


        if(rogersPlan != null)
        {
            return rogersPlan;
        }
        else{

            List<Plan> planList = webScraperService.scrapePlans();
            redisService.setValue("rogers", planList, 300l);
            return planList;

        }
    }

}
