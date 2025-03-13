package com.yash.api_virgin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class centralService
{

    @Autowired
    private redisService redisService;

    @Autowired
    private PlanScraperService planScraperService;

    public List<Map<String,String>> getPrepaidPlans()
    {
        List<Map<String,String>> virginplans = redisService.getValue("virgin", new TypeReference<List<Map<String,String>>>() {
        });


        if(virginplans != null)
        {
            return virginplans;
        }
        else{

            List<Map<String,String>> planList = null;
            try {
                planList = planScraperService.scrapePlans();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            redisService.setValue("virgin", planList, 300l);
            return planList;

        }
    }

}
