package com.tyfanchz.designpattern.behavioral.command;

public class AssociationDepartment implements Department {
    @Override
    public void find() {
        System.out.println("找到组织部");
    }

    @Override
    public void createIssue() {
        System.out.println("向组织部提出需求");
    }

    @Override
    public void modifyIssue() {
        System.out.println("修改组织部需求");
    }

    @Override
    public void removeIssue() {
        System.out.println("让组织部移除需求");
    }

    @Override
    public void plan() {
        System.out.println("组织部提上日程");
    }
}
