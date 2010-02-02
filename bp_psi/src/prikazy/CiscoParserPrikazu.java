/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package prikazy;

import java.util.LinkedList;
import pocitac.AbstractPocitac;
import pocitac.Konsole;

/**
 *
 * @author haldyr
 */
public class CiscoParserPrikazu extends ParserPrikazu {

    public CiscoParserPrikazu(AbstractPocitac pc, Konsole kon) {
        super(pc, kon);
    }

    @Override
    public void zpracujRadek(String s) {
        
        AbstraktniPrikaz pr;
        radek = s;
        slova = new LinkedList<String>();

        //rozsekej();
        rozsekejLepe();

        if (slova.size() < 1) {
            return;
        }

        if (slova.get(0).equals("")) {
            return; // prazdny Enter
        }

        if (slova.get(0).equals("exit")) {
            pr = new Exit(pc, kon, slova);
        } else if (slova.get(0).equals("ifconfig")) {
            pr = new Ifconfig(pc, kon, slova);
        } else {
            kon.posliRadek("% Unknown command or computer name, or unable to find computer address");
        }
    }
}
