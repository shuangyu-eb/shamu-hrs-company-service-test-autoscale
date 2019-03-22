package com.tardisone.companyservice.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldCheckUtil {

    public static boolean checkFieldByRegex(String field, String regex){
        if(null == field || "".equals(field)){
            return true;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        return matcher.matches();
    }

    public static boolean checkNotNullOrEmpty(String field){
        if(null == field || "".equals(field)){
            return false;
        }
        return true;
    }

    public static boolean checkAllField(List<Map<String, String>> fieldMapList){
        boolean checkFlag = true;
        for(Map fieldValueMap : fieldMapList){
            String fieldValue = (String) fieldValueMap.get("value");
            String regex = (String) fieldValueMap.get("regex");

            checkFlag = checkFieldByRegex(fieldValue, regex);
            if(checkFlag == false){
                break;
            }

        }
        return checkFlag;
    }

    public static Map<String, String> getFieldValueMap(HttpServletRequest request, String fieldName, String regex){
        String fieldValue = request.getParameter(fieldName);
        Map<String, String> valueMap = new HashMap<String, String>();
        valueMap.put("value", fieldValue);
        valueMap.put("regex", regex);
        return valueMap;
    }
}
