package com.tyfanchz.designpattern.behavioral.command;

public class AddFinanceCommand extends Command {
    @Override
    public void execute() {
        super.financeDepartment.find();
        super.financeDepartment.createIssue();
        super.financeDepartment.plan();
    }
}
