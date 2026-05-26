package com.tyfanchz.designpattern.behavioral.command;

public class CommandTest {
    public static void main(String[] args) {
        testCommand();
    }

    private static void testCommand() {
        Represent represent = new MeRepresent();
        Command addFinanceCommand = new AddFinanceCommand();
        Command removeAssociationCommand = new RemoveAssociationCommand();

        represent.action(addFinanceCommand);
        represent.action(removeAssociationCommand);
    }
}
