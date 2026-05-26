package com.tyfanchz.designpattern.structural.decorator;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class ItemRoomDecorator extends AbstractRoomDecorator {
    public ItemRoomDecorator(Room room) {
        super(room);
    }

    @Override
    public void show() {
        this.showItem();
        super.show();
    }

    private void showItem() {
        System.out.println("房间里还有各种必需品和小东西...");
    }
}
