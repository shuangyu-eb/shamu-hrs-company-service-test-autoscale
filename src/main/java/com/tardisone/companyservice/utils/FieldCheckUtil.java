package com.tardisone.companyservice.utils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    public static boolean checkAllFields(HttpServletRequest request){
        List<Map<String,String>> checkList = new ArrayList<>();

        Map<String, String> firstNameMap = FieldCheckUtil.getFieldValueMap(request, Constants.FIRST_NAME, Constants.PERSONNAME_REGEX);
        Map<String, String> lastNameMap = FieldCheckUtil.getFieldValueMap(request, Constants.FIRST_NAME, Constants.PERSONNAME_REGEX);
        Map<String, String> workEmailMap = FieldCheckUtil.getFieldValueMap(request, Constants.WORK_EMAIL, Constants.EMAIL_REGEX);
        Map<String, String> personalEmailMap = FieldCheckUtil.getFieldValueMap(request, Constants.PERSONAL_EMAIL, Constants.EMAIL_REGEX);
        Map<String, String> phoneWorkMap = FieldCheckUtil.getFieldValueMap(request, Constants.WORK_PHONE, Constants.PHONE_NUMBER_REGEX);
        Map<String, String> phonePersonalMap = FieldCheckUtil.getFieldValueMap(request, Constants.HOME_PHONE, Constants.PHONE_NUMBER_REGEX);

        Map<String, String> jobTitleIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.JOB_TITLE, Constants.NORMAL_REGEX);
        Map<String, String> employeeTypeMap = FieldCheckUtil.getFieldValueMap(request, Constants.EMPLOYEE_TYPE, Constants.INTEGER_REGEX);
        Map<String, String> hireDateMap = FieldCheckUtil.getFieldValueMap(request, Constants.HIRE_DATE, Constants.NORMAL_REGEX);
        Map<String, String> managerIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.REPORTS_TO, Constants.PHONE_NUMBER_REGEX);
        Map<String, String> departmentIdMap = FieldCheckUtil.getFieldValueMap(request, Constants.DEPARTMENT, Constants.INTEGER_REGEX);
        Map<String, String> compensationMap = FieldCheckUtil.getFieldValueMap(request, Constants.COMPENSATION, Constants.LONG_INTEGER_REGEX);
        Map<String, String> compensationUnitMap = FieldCheckUtil.getFieldValueMap(request, Constants.COMPENSATION_UNIT, Constants.NORMAL_REGEX);
        Map<String, String> officeLocationMap = FieldCheckUtil.getFieldValueMap(request, Constants.OFFICE_LOCATION, Constants.NORMAL_REGEX);

        checkList.add(firstNameMap);
        checkList.add(lastNameMap);
        checkList.add(workEmailMap);
        checkList.add(personalEmailMap);
        checkList.add(phoneWorkMap);
        checkList.add(phonePersonalMap);

        checkList.add(jobTitleIdMap);
        checkList.add(employeeTypeMap);
        checkList.add(phonePersonalMap);
        checkList.add(hireDateMap);
        checkList.add(managerIdMap);
        checkList.add(departmentIdMap);
        checkList.add(compensationMap);
        checkList.add(compensationUnitMap);
        checkList.add(officeLocationMap);

        return FieldCheckUtil.checkAllField(checkList);
    }

    public static Timestamp getTimestampFromString(String date){
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Timestamp timestamp = null;
        try {
            if(!"".equals(date) && null != date){
                Date simpleDate = dateFormat.parse(date);
                timestamp = new Timestamp(simpleDate.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return timestamp;
        }
    }
}
