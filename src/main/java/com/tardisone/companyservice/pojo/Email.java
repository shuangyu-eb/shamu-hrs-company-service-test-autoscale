package com.tardisone.companyservice.pojo;

import lombok.Data;

@Data
public class Email {
    private String from;

    private String to;

    private String subject;

    private String content;

    public Email(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }
}
