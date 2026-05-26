package com.tyfanchz.designpattern.behavioral.command;

public class FinanceDepartment implements Department {
    @Override
    public void find() {
        System.out.println("找到财务部人员");
    }

    @Override
    public void createIssue() {
        System.out.println("提出财务需求");
    }

    @Override
    public void modifyIssue() {
        System.out.println("修改财务需求");
    }

    @Override
    public void removeIssue() {
        System.out.println("移除财务需求");
    }

    @Override
    public void plan() {
        System.out.println("财务需求提上日程");
    }
}
