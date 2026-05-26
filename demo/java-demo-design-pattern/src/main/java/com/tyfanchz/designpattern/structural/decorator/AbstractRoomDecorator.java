package com.tyfanchz.designpattern.structural.decorator;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public abstract class AbstractRoomDecorator implements Room {
    private Room room;

    public AbstractRoomDecorator(Room room) {
        this.room = room;
    }

    @Override
    public void show() {
        this.room.show();
    }
}
