package com.tyfanchz.designpattern.creational.prototype;

import java.io.Serializable;
import com.tyfanchz.designpattern.utils.TyzObjectUtils;

public class Mail implements Serializable, Cloneable {
    private String subject;
    private String receiver;
    private String content;

    public Mail(MailTemplate mailTemplate) {
        this.subject = mailTemplate.getSubject();
        this.content = mailTemplate.getContent();
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    protected Object clone() {
        Mail clonedMail;

        clonedMail = TyzObjectUtils.deepClone(this);

        return clonedMail;
    }

    @Override
    public String toString() {
        return "Mail{" +
            "subject='" + this.subject + '\'' +
            ", receiver='" + this.receiver + '\'' +
            ", content='" + this.content + '\'' +
            '}';
    }
}
