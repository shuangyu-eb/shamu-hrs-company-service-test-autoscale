package shamu.company.utils;

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

        Map<String, String> firstNameMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.FIRST_NAME, JobUserConstants.PERSONNAME_REGEX);
        Map<String, String> lastNameMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.FIRST_NAME, JobUserConstants.PERSONNAME_REGEX);
        Map<String, String> workEmailMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.WORK_EMAIL, JobUserConstants.EMAIL_REGEX);
        Map<String, String> personalEmailMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.PERSONAL_EMAIL, JobUserConstants.EMAIL_REGEX);
        Map<String, String> phoneWorkMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.WORK_PHONE, JobUserConstants.PHONE_NUMBER_REGEX);
        Map<String, String> phonePersonalMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.HOME_PHONE, JobUserConstants.PHONE_NUMBER_REGEX);

        Map<String, String> jobTitleIdMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.JOB_TITLE, JobUserConstants.NORMAL_REGEX);
        Map<String, String> employeeTypeMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.EMPLOYEE_TYPE, JobUserConstants.INTEGER_REGEX);
        Map<String, String> hireDateMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.HIRE_DATE, JobUserConstants.NORMAL_REGEX);
        Map<String, String> managerIdMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.REPORTS_TO, JobUserConstants.PHONE_NUMBER_REGEX);
        Map<String, String> departmentIdMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.DEPARTMENT, JobUserConstants.INTEGER_REGEX);
        Map<String, String> compensationMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.COMPENSATION, JobUserConstants.LONG_INTEGER_REGEX);
        Map<String, String> compensationUnitMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.COMPENSATION_UNIT, JobUserConstants.NORMAL_REGEX);
        Map<String, String> officeLocationMap = FieldCheckUtil.getFieldValueMap(request, JobUserConstants.OFFICE_LOCATION, JobUserConstants.NORMAL_REGEX);

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
