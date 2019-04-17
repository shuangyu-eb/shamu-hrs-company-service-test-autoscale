package com.tardisone.companyservice.pojo;

import com.tardisone.companyservice.entity.Office;
import com.tardisone.companyservice.entity.OfficeAddresses;
import lombok.Data;

import java.util.Date;
@Data
public class JobInfomationPojo {
    private String jobTitle;

    private String employmentType;

    Date hireDate;

    private String manager;

    private String department;

    private String compensation;

    private String officeName;

    private OfficeAddresses officeAddresses;

    public JobInfomationPojo(){

    }

    public JobInfomationPojo(String jobTitle, String employmentType, Date hireDate, String manager, String department, String compensation, String officeName,OfficeAddresses officeAddresses) {
        this.jobTitle = jobTitle;
        this.employmentType = employmentType;
        this.hireDate = hireDate;
        this.manager = manager;
        this.department = department;
        this.compensation = compensation;
        this.officeName=officeName;
        this.officeAddresses = officeAddresses;
    }

}
