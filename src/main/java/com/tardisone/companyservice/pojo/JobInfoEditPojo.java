package com.tardisone.companyservice.pojo;

import lombok.Data;

@Data
public class JobInfoEditPojo {
   private Integer compensation;
   private Long employmentType;
   private Long frequency;
   private String hireDate;
   private String jobTitle;
   private Long manager;
   private Long location;
   private Long userId;
}
