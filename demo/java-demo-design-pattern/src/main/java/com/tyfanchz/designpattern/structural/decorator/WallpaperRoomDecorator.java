package com.tyfanchz.designpattern.structural.decorator;

/**
 * <p>Description:
 *
 * <p>Project: DesignPattern
 *
 * @author tyfanchz
 * @date 2020-04-07
 */
public class WallpaperRoomDecorator extends AbstractRoomDecorator {
    public WallpaperRoomDecorator(Room room) {
        super(room);
    }

    @Override
    public void show() {
        this.showWallpaper();
        super.show();
    }

    private void showWallpaper() {
        System.out.println("墙上有精美的贴纸...");
    }
}
