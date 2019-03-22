package com.tardisone.companyservice.model;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_number")
    private Integer employeeNumber;

    @Column(name = "email_work")
    private String emailWork;

    @Column(name = "password")
    private String password;

    @Column(name = "last_login")
    private String lastLogin;

    @Column(name = "user_status_id")
    private Integer userStatusId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "manager_user_id")
    private Integer managerUserId;

    @Column(name = "user_personal_information_id")
    private Integer userPersonalInformationId;

    @Column(name = "user_contact_information_id")
    private Integer userContactInformationId;

    @Column(name = "user_compensation_id")
    private Integer userCompensationId;

    @Column(name = "user_role_id")
    private Integer userRoleId;

    @Column(name = "invitation_email_token")
    private String invitationEmailToken;

    @Column(name = "invited_at")
    private String invitedAt;

    @Column(name = "reset_password_sent_at")
    private String resetPasswordSentAt;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verified_at")
    private String verifiedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(Integer employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmailWork() {
        return emailWork;
    }

    public void setEmailWork(String emailWork) {
        this.emailWork = emailWork;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getUserStatusId() {
        return userStatusId;
    }

    public void setUserStatusId(Integer userStatusId) {
        this.userStatusId = userStatusId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getManagerUserId() {
        return managerUserId;
    }

    public void setManagerUserId(Integer managerUserId) {
        this.managerUserId = managerUserId;
    }

    public Integer getUserPersonalInformationId() {
        return userPersonalInformationId;
    }

    public void setUserPersonalInformationId(Integer userPersonInformationId) {
        this.userPersonalInformationId = userPersonInformationId;
    }

    public Integer getUserContactInformationId() {
        return userContactInformationId;
    }

    public void setUserContactInformationId(Integer userContactInformationId) {
        this.userContactInformationId = userContactInformationId;
    }

    public Integer getUserCompensationId() {
        return userCompensationId;
    }

    public void setUserCompensationId(Integer userCompensationId) {
        this.userCompensationId = userCompensationId;
    }

    public Integer getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(Integer userRoleId) {
        this.userRoleId = userRoleId;
    }

    public String getInvitationEmailToken() {
        return invitationEmailToken;
    }

    public void setInvitationEmailToken(String invitationEmailToker) {
        this.invitationEmailToken = invitationEmailToker;
    }

    public String getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(String invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getResetPasswordSentAt() {
        return resetPasswordSentAt;
    }

    public void setResetPasswordSentAt(String resetPasswordSentAt) {
        this.resetPasswordSentAt = resetPasswordSentAt;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
