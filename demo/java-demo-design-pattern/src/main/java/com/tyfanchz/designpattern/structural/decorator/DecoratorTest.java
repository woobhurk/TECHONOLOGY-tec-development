package com.tyfanchz.designpattern.structural.decorator;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class DecoratorTest {
    public static void main(String[] args) {
        testDecorator();
    }

    private static void testDecorator() {
        RawRoom rawRoom = new RawRoom();
        WallpaperRoomDecorator wallpaperRoom = new WallpaperRoomDecorator(rawRoom);
        ItemRoomDecorator itemRoom = new ItemRoomDecorator(wallpaperRoom);

        itemRoom.show();
    }
}
