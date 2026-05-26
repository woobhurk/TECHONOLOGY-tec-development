package com.tyfanchz.designpattern.creational.prototype;

import java.io.Serializable;

public interface MailTemplate extends Serializable, Cloneable {
    String getSubject();

    String getContent();
}
