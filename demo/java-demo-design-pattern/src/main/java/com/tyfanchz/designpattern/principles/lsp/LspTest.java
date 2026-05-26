package com.tyfanchz.designpattern.principles.lsp;

public class LspTest {
    public static void main(String[] args) {
        Human noName = new Human("NoName", new HpLaptop());

        noName.usingLaptop();
        noName.setLaptop(new AsuaLaptop());
        noName.usingLaptop();
    }
}
