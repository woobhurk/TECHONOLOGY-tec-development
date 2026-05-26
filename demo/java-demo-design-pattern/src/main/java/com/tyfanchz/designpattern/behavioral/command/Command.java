package com.tyfanchz.designpattern.behavioral.command;

public abstract class Command {
    protected Department financeDepartment = new FinanceDepartment();
    protected Department associationDepartment = new AssociationDepartment();

    public abstract void execute();
}
