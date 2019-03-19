package com.tardisone.companyservice.model;

public class User {

    private Integer id;

    private Integer employeeNumber;

    private String emailWork;

    private String password;

    private String lastLogin;

    private Integer userStatusId;

    private String imageUrl;

    private Integer companyId;

    private Integer managerUserId;

    private Integer userPersonInformationId;

    private Integer userContactInformationId;

    private Integer userCompensationId;

    private Integer userRoleId;

    private String invitationEmailToker;

    private String invitedAt;

    private String resetPasswordSentAt;

    private String resetPasswordToken;

    private String verificationToken;

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

    public Integer getUserPersonInformationId() {
        return userPersonInformationId;
    }

    public void setUserPersonInformationId(Integer userPersonInformationId) {
        this.userPersonInformationId = userPersonInformationId;
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

    public String getInvitationEmailToker() {
        return invitationEmailToker;
    }

    public void setInvitationEmailToker(String invitationEmailToker) {
        this.invitationEmailToker = invitationEmailToker;
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
