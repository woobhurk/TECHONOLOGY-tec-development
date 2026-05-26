package com.tyfanchz.designpattern.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@SuppressWarnings("unchecked")
public class TyzObjectUtils {
    private TyzObjectUtils() {}

    public static <T> T deepClone(T obj) {
        T clonedObj;
        ByteArrayOutputStream byteArrayOutputStream;
        ObjectOutputStream objectOutputStream;
        ByteArrayInputStream byteArrayInputStream;
        ObjectInputStream objectInputStream;

        try {
            // 写入输出流
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            // 从输出流读取
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            clonedObj = (T) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            clonedObj = null;
        }

        return clonedObj;
    }
}
