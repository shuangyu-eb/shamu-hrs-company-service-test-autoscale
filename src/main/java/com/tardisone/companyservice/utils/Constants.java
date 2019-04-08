package com.tardisone.companyservice.utils;

public class Constants {

    public static final String PERSON_NAME = "personName";

    public static final String FIRST_NAME= "firstName";

    public static final String LAST_NAME = "lastName";

    public static final String WORK_EMAIL = "workEmail";

    public static final String PERSONAL_EMAIL = "personalEmail";

    public static final String JOB_TITLE = "jobTitle";

    public static final String EMPLOYEE_TYPE = "employeeType";

    public static final String HIRE_DATE = "hireDate";

    public static final String REPORTS_TO = "reportsTo";

    public static final String DEPARTMENT = "department";

    public static final String COMPENSATION = "compensation";

    public static final String COMPENSATION_UNIT = "compensationUnit";

    public static final String OFFICE_LOCATION = "officeLocation";

    public static final String WORK_PHONE = "workPhone";

    public static final String HOME_PHONE = "personalPhone";


    //////////////
    public static final String PERSONNAME_REGEX = "^[A-Za-z0-9\\u4e00-\\u9fa5]+$";

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";

//    public static final String PHONE_NUMBER_REGEX = "^\\+[0-9]{0,3}\\S[0-9]*$";

    public static final String PHONE_NUMBER_REGEX = "^[0-9]*$";

    public static final String LONG_INTEGER_REGEX = "^[0-9]+[\\.]?[0-9]{0,2}$";

    public static final String INTEGER_REGEX = "^[0-9]$";

    public static final String NORMAL_REGEX = "^[a-zA-Z0-9\\s]*$";


}
