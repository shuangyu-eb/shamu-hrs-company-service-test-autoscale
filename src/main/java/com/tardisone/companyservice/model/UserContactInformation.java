package com.tardisone.companyservice.model;

import javax.persistence.*;

@Entity
@Table(name = "user_contact_information")
public class UserContactInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "phone_work")
    private String phoneWork;

    @Column(name = "phone_work_extension")
    private Integer phoneWorkExtension;

    @Column(name = "phone_mobile")
    private String phoneMobile;

    @Column(name = "phone_home")
    private String phoneHome;

    @Column(name = "email_work")
    private String emailWork;

    @Column(name = "email_home")
    private String emailHome;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public Integer getPhoneWorkExtension() {
        return phoneWorkExtension;
    }

    public void setPhoneWorkExtension(Integer phoneWorkExtension) {
        this.phoneWorkExtension = phoneWorkExtension;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public void setPhoneMobile(String phoneMobile) {
        this.phoneMobile = phoneMobile;
    }

    public String getPhoneHome() {
        return phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getEmailWork() {
        return emailWork;
    }

    public void setEmailWork(String emailWork) {
        this.emailWork = emailWork;
    }

    public String getEmailHome() {
        return emailHome;
    }

    public void setEmailHome(String emailHome) {
        this.emailHome = emailHome;
    }
}
