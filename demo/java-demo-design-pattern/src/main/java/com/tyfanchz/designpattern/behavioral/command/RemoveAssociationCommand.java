package com.tyfanchz.designpattern.behavioral.command;

public class RemoveAssociationCommand extends Command {
    @Override
    public void execute() {
        super.associationDepartment.find();
        super.associationDepartment.removeIssue();
        super.associationDepartment.plan();
    }
}
