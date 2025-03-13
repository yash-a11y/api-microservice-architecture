package com.yash.api_virgin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class redisService {

    @Autowired
    private RedisTemplate redisTemplate;


    public <T> T getValue(String key, TypeReference<T> typeRef)
    {

        try {
            String json = (String) redisTemplate.opsForValue().get(key);
            if(json == null) return null;
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {

            return null;
        }

    }

    public void setValue(String key, Object o, Long ttl)
    {
        try{

            ObjectMapper mapper = new ObjectMapper();
            String jsonval = mapper.writeValueAsString(o);

            redisTemplate.opsForValue().set(key,jsonval,ttl, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {

        }
    }

}
