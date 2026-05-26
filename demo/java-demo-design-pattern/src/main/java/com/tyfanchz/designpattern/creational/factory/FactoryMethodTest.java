package com.tyfanchz.designpattern.creational.factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryMethodTest {
    public static void main(String[] args) {
        testGunKiller();
        testKnifeKiller();
    }

    private static void testGunKiller() {
        KillerCreator killerCreator = new KillerCreatorImpl();
        Killer killer = killerCreator.create(GunKiller.class);

        killer.killOne("赵XX & 周XX");
    }

    private static void testKnifeKiller() {
        KillerCreator killerCreator = new KillerCreatorImpl();
        KnifeKiller killer = killerCreator.create(KnifeKiller.class);
        List<String> nameList = new ArrayList<>();

        nameList.add("周XX");
        nameList.add("赵XX");
        killer.killMore(nameList);
    }
}
