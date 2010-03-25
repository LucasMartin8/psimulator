package prikazy;

import datoveStruktury.CiscoStavy;
import datoveStruktury.IpAdresa;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static datoveStruktury.CiscoStavy.*;
import java.util.LinkedList;
import java.util.List;
import pocitac.AbstractPocitac;
import pocitac.Konsole;
import pocitac.SitoveRozhrani;
import vyjimky.ZakazanaIpAdresaException;

/**
 * Parser prikazu pro cisco, zde se volaji prikazy dle toho, co poslal uzivatel.
 * @author haldyr
 */
public class CiscoParserPrikazu extends ParserPrikazu {

    public CiscoParserPrikazu(AbstractPocitac pc, Konsole kon) {
        super(pc, kon);
        slova = new LinkedList<String>();
    }
    /**
     * Stav, ve kterem se aktualne nachazi cisco.
     */
    CiscoStavy stav = USER;
    /**
     * Specialni formatovac casu pro vypis servisnich informaci z cisca.
     */
    DateFormat formator = new SimpleDateFormat("MMM  d HH:mm:ss.SSS");
    /**
     * Indikuje stav, kdy uzivatel napise jen configure. Pak se mu nevypise promt, ale staci zmacknout Enter
     * pro potvrzeni prikazu 'configure terminal'
     */
    boolean configure1 = false;
    /**
     * Rozhrani, ktere se bude upravovat ve stavu IFACE
     */
    SitoveRozhrani aktualni = null;
    /**
     * Pomocna promenna pro zachazeni s '% Ambiguous command: '
     */
    boolean nepokracovat = false;
    /**
     * Chyba, ktera se vypise pri '% Ambiguous command: '
     */
    String chybovyVypis = "";

    private void ping() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Tato metoda simuluje zkracovani prikazu tak, jak cini cisco. 
     * @param command prikaz, na ktery se zjistuje, zda lze na nej doplnit. 
     * @param cmd prikaz, ktery zadal uzivatel
     * @return Vrati true, pokud retezec cmd je jedinym moznym prikazem, na ktery ho lze doplnit.
     */
    private boolean kontrola(String command, String cmd) {

        int i = 10;

        // Zde jsou zadefinovany vsechny prikazy. Jsou rozdeleny do poli podle poctu znaku,
        // ktere je potreba k jejich bezpecne identifikaci. Cisla byla ziskana z praveho cisca.
        String[] jedna = {"show", "terminal", "inside", "outside", "source", "static", "pool", "netmask", "permit"};
        // + ip, exit
        String[] dva = {"interface", "address", "no", "shutdown", "enable", "classless", "access-list", "ping"};
        // + ip, exit
        String[] tri = {"running-config", "name-server", "nat"};
        // + exit
        String[] ctyri = {"configure", "disable"};
        String[] pet = {"route"};

        List<String[]> seznam = new ArrayList<String[]>();
        seznam.add(jedna);
        seznam.add(dva);
        seznam.add(tri);
        seznam.add(ctyri);
        seznam.add(pet);

        int n = 0;
        for (String[] pole : seznam) { // nastaveni spravne delky dle zarazeni do seznamu
            n++;
            for (String s : pole) {
                if (s.equals(command)) {
                    i = n;
                }
            }
        }

        if (command.equals("exit")) { // specialni chovani prikazu exit v ruznych stavech
            switch (stav) {
                case USER:
                case ROOT:
                    i = 2;
                    break;
                case CONFIG:
                    i = 3;
                    break;
                case IFACE:
                    i = 1;
            }
        }

        if (command.equals("ip")) { // specialni chovani prikazu ip v ruznych stavech
            switch (stav) {
                case CONFIG:
                    i = 2;
                    break;
                case IFACE:
                    i = 1;
            }
        }

        if (cmd.length() >= i && command.startsWith(cmd)) { // lze doplnit na jeden jedinecny prikaz
            return true;
        }

        if (command.startsWith(cmd)) {
            chybovyVypis = "% Ambiguous command:  \"";

            if (command.equals("show")) {
                chybovyVypis += "show\"\n";
            } else if (command.equals("running-config")) {
                chybovyVypis += "show " + cmd + "\"\n";
            } else {
                chybovyVypis += cmd + "\"\n";
            }

            nepokracovat = true;
        }

        return false;
    }

    /**
     * V teto metode se vola runningconfig(). Kdyz je spatny vstup (tj. jiny nez 'show running-config' ve stavu ROOT),
     * tak to vyvola chybovou hlasku.
     */
    private void show() {

        switch (stav) {

            case ROOT:
                if (slova.size() == 2) {
//                    System.out.println("tady: " + slova.get(1));
                    if (kontrola("running-config", slova.get(1))) {
                        runningconfig();
                        return;
                    }
                    if (nepokracovat) {
                        kon.posli(chybovyVypis);
                    }
                } else if (slova.size() == 1) {
                    kon.posliRadek("% Type \"show ?\" for a list of subcommands\n");
                    return;
                }
                break;

            case USER:
            // TODO: tady asi jeste neco bude?

            default:
                invalidInputDetected();
        }
    }

    /**
     * Prepina cisco do stavu config (CONFIG).
     */
    private void configure() {

        if (slova.size() == 1 && !configure1) {
            kon.posli("Configuring from terminal, memory, or network [terminal]? ");
            kon.vypisPrompt = false;
            configure1 = true;
            return;
        }

        int cis = 1;
        if (configure1) {
            cis = 0;
        }
        if (kontrola("terminal", slova.get(cis)) || configure1) {
            //if (slova.get(cis).equals("terminal") || configure1) {
            stav = CONFIG;
            kon.prompt = pc.jmeno + "(config)#";
            kon.posliRadek("Enter configuration commands, one per line.  End with 'exit'."); // zmena oproti ciscu: End with CNTL/Z.
            configure1 = false;
            kon.vypisPrompt = true;
            return;
        }

        int pocet = pc.jmeno.length() + 1 + slova.get(0).length() + 1;
        String ret = "";

        for (int i = 0; i < pocet; i++) {
            ret += " ";
        }
        ret += "^";
        kon.posliRadek(ret);
        kon.posliRadek("% Invalid input detected at '^' marker.");
        kon.posliRadek("");
    }

    private void iproute() {

        // ip route 'cil' 'maska cile' 'kam poslat'
        // ip route 0.0.0.0 0.0.0.0 192.168.2.254
        // ip route 192.168.2.0 255.255.255.192 fastEthernet 0/0
        // ip route 192.168.2.0 255.255.255.192 fastEthernet0/0

        if (slova.size() < 5) {
            incompleteCommand();
            return;
        }

        IpAdresa cil = new IpAdresa(slova.get(2), slova.get(3));

        if (slova.size() == 6 || (slova.size() == 5 && ! zacinaCislem(slova.get(4)))) {
            // ip route 192.168.2.0 255.255.255.192 fastEthernet 0/0

            SitoveRozhrani sr = null;
            String rozhrani;
            if (slova.size() == 6) {
                rozhrani = slova.get(4) + slova.get(5);
            } else {
                rozhrani = slova.get(4);
            }
            
            for (SitoveRozhrani iface : pc.rozhrani) {
                if (iface.jmeno.equalsIgnoreCase(rozhrani)) {
                    sr = iface;
                }
            }
            if (sr == null) {
                invalidInputDetected();
                return;
            }

            pc.routovaciTabulka.pridejZaznamBezKontrol(cil, null, sr);
            
        } else if (slova.size() == 5) { // ip route 0.0.0.0 0.0.0.0 192.168.2.254
            IpAdresa brana = new IpAdresa(slova.get(4));
//            pc.routovaciTabulka.
            //TODO: tady pokracovat - vyzkoumat, jak se chova cisco
            pc.routovaciTabulka.pridejZaznamBezKontrol(cil, brana, null);
        } else {
            invalidInputDetected();
        }
    }

    private boolean zacinaCislem(String s) {

        String pismeno = s.substring(0, 1);
        int i = -1;

        try {
            i = Integer.parseInt(pismeno);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * interface fastEthernet0/1
     * Prepne cisco do stavu config-if (IFACE).
     * Kdyz ma prikaz interface 2 argumenty, tak se sloucej do jednoho (pripad: interface fastEthernet 0/0).
     * 0 nebo vice nez argumenty znamena chybovou hlasku.
     * Do globalni promenne 'aktualni' uklada referenci na rozhrani, ktere chce uzivatel konfigurovat.
     */
    private void iface() {

        String rozhrani = "";
        switch (slova.size()) {
            case 1:
                incompleteCommand();
                return;

            case 2:
                rozhrani = slova.get(1);
                break;

            case 3:
                rozhrani = slova.get(1) + slova.get(2);
                break;

            default:
                invalidInputDetected();
                return;
        }

        boolean nalezeno = false;
        for (SitoveRozhrani iface : pc.rozhrani) {
            if (iface.jmeno.equalsIgnoreCase(rozhrani)) {
                aktualni = iface;
                nalezeno = true;
            }
        }

        if (nalezeno == false) {
            invalidInputDetected();
            return;
        }

        stav = IFACE;
        kon.prompt = pc.jmeno + "(config-if)#";
    }

    /**
     * Vypise chybovou hlasku pri zadani nekompletniho prikazu.
     */
    private void incompleteCommand() {
        kon.posliRadek("% Incomplete command.");
    }

    /**
     * Vypise chybovou hlasku pri zadani neplatneho vstupu.
     */
    private void invalidInputDetected() {
        kon.posliRadek("\n% Invalid input detected.\n");
    }

    private void accesslist() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Tento prikaz zapne rozhrani, ktere je definovano v aktualnim nastovacim rezimu (napr.: interface fastEthernet0/0)
     */
    private void noshutdown() {
        if (slova.size() != 2) {
            incompleteCommand();
            return;
        }
        if (kontrola("shutdown", slova.get(1))) {
            //if (slova.get(1).equals("shutdown")) {
            if (aktualni.vratStavRozhrani() == false) { // kdyz nahazuju rozhrani
                Date d = new Date();
                kon.posliRadek(formator.format(d) + ": %LINK-3-UPDOWN: Interface " + aktualni.jmeno + ", changed state to up");
                kon.posliRadek(formator.format(d) + ": %LINEPROTO-5-UPDOWN: Line protocol on Interface " + aktualni.jmeno + ", changed state to up");
            }

            aktualni.nastavRozhrani(true);
        }
        if (nepokracovat) {
            kon.posliRadek("% Ambiguous command:  \"" + radek + "\"");
        }
    }

    @Override
    public void zpracujRadek(String s) {

        AbstraktniPrikaz prikaz;
        radek = s;
        slova.clear();
        nepokracovat = false;
        chybovyVypis = "";

        rozsekejLepe();

        if (slova.size() < 1) {
            return; // jen mezera
        }
//        kon.posliRadek("Doplnovani: '"+radek+"'");

        String prvniSlovo = slova.get(0);

        if (configure1) {
            if (kontrola("terminal", prvniSlovo) || prvniSlovo.equals("")) {
//            if (slova.get(0).equals("terminal") || slova.get(0).equals("")) {
                configure();
                return;
            } else {
                kon.posliRadek("?Must be \"terminal\"");
                return;
            }
        }

        if (prvniSlovo.equals("")) {
            return; // prazdny Enter
        }

        boolean nepokracuj = spolecnePrikazy();
        if (nepokracuj) {
            return;
        }

        if (prvniSlovo.equals("?")) {
            prikaz = new Otaznik(pc, kon, slova, stav);
            return;
        }

// == stavy ==
// user -                           ping, enable
// root - enable/disable            ping, enable, configure, reload, show    (jmeno#)
// konfiguracni rezim - configure terminal/exit             ip route, interface, access-list, exit   (jmeno(config)#)
// konfigurace interface - interface/exit                   ip address, no shutdown, exit   (jmeno(config-if)#)


        switch (stav) {
            case USER:
                if (kontrola("enable", prvniSlovo)) {
                    stav = ROOT;
                    kon.prompt = pc.jmeno + "#";
                    return;
                }
                if (kontrola("ping", prvniSlovo)) {
                    ping();
                    return;
                }
                if (kontrola("show", prvniSlovo)) {
                    show();
                    return;
                }
                if (kontrola("exit", prvniSlovo)) {
                    kon.ukonciSpojeni();
                    return;
                }
                break;

            case ROOT:
                if (kontrola("enable", prvniSlovo)) { // funguje v cisco taky, ale nic nedela
                    return;
                }
                if (kontrola("disable", prvniSlovo)) {
                    stav = USER;
                    kon.prompt = pc.jmeno + ">";
                    return;
                }
                if (kontrola("ping", prvniSlovo)) {
                    ping();
                    return;
                }
                if (kontrola("configure", prvniSlovo)) {
                    configure();
                    return;
                }
                if (kontrola("show", prvniSlovo)) {
                    show();
                    return;
                }
                break;

            //ip route, interface, access-list, exit
            case CONFIG:
                if (kontrola("exit", prvniSlovo)) {
                    stav = ROOT;
                    kon.prompt = pc.jmeno + "#";
                    Date d = new Date();
                    kon.posliRadek(formator.format(d) + ": %SYS-5-CONFIG_I: Configured from console by console");
                    return;
                }
                if (kontrola("ip", prvniSlovo)) {
                    iproute();
                    return;
                }
                if (kontrola("interface", prvniSlovo)) {
                    iface();
                    return;
                }
                if (kontrola("access-list", prvniSlovo)) {
                    accesslist();
                    return;
                }
                break;

            //ip address, no shutdown, exit   (jmeno(config-if)#)
            case IFACE:
                if (kontrola("exit", prvniSlovo)) {
                    stav = CONFIG;
                    kon.prompt = pc.jmeno + "(config)#";
                    aktualni = null; // zrusime odkaz na menene rozhrani
                    return;
                }
                if (kontrola("ip", prvniSlovo)) {
                    ipaddress();
                    return;
                }
                if (kontrola("no", prvniSlovo)) {
                    noshutdown();
                    return;
                }
        }

        if (slova.get(0).equals("ifconfig")) { // pak smazat
            prikaz = new Ifconfig(pc, kon, slova);
        } else if (slova.get(0).equals("route")) {
            prikaz = new LinuxRoute(pc, kon, slova);
        } else {

            if (nepokracovat) {
                kon.posli(chybovyVypis);
                nepokracovat = false;
                chybovyVypis = "";
                return;
            }

            switch (stav) {
                case CONFIG:
                case IFACE:
                    invalidInputDetected();
                    break;

                default:
                    kon.posliRadek("% Unknown command or computer name, or unable to find computer address");

            }


        }
    }

    /**
     * Prikaz 'show running-config' ve stavu # (ROOT).
     * Aneb vypis rozhrani v uplne silenem formatu.
     */
    private void runningconfig() {
        kon.posliRadek("Building configuration...\n"
                + "\n"
                + "Current configuration : 827 bytes\n"
                + "!\n"
                + "version 12.4\n"
                + "service timestamps debug datetime msec\n"
                + "service timestamps log datetime msec\n"
                + "no service password-encryption\n"
                + "!\n"
                + "hostname " + pc.jmeno + "\n"
                + "!\n"
                + "boot-start-marker\n"
                + "boot-end-marker\n"
                + "!\n"
                + "!\n"
                + "no aaa new-model\n"
                + "!\n"
                + "resource policy\n"
                + "!\n"
                + "mmi polling-interval 60\n"
                + "no mmi auto-configure\n"
                + "no mmi pvc\n"
                + "mmi snmp-timeout 180\n"
                + "ip subnet-zero\n"
                + "ip cef\n"
                + "!\n"
                + "!\n"
                + "no ip dhcp use vrf connected\n"
                + "!\n"
                + "!\n"
                + "ip name-server 147.32.80.9\n"
                + "ip name-server 147.32.80.105\n"
                + "!\n"
                + "!\n"
                + "!\n"
                + "!");
        for (Object o : pc.rozhrani) {
            SitoveRozhrani sr = (SitoveRozhrani) o;
            kon.posliRadek("interface " + sr.jmeno + "\n"
                    + " ip address " + sr.ip.vypisIP() + " " + sr.ip.vypisMasku());
            if (sr.vratStavRozhrani() == false) {
                kon.posliRadek(" shutdown");
            }
            kon.posliRadek(" duplex auto\n"
                    + " speed auto\n!");
        }

        kon.posliRadek(pc.routovaciTabulka.vypisSeCiscove());

        kon.posliRadek("!\n");

        kon.posliRadek("ip http server\n"
                + "!\n"
                + "!\n"
                + "control-plane\n"
                + "!\n"
                + "!\n"
                + "line con 0\n"
                + "line aux 0\n"
                + "line vty 0 4\n"
                + " login\n"
                + "!\n"
                + "end\n");
    }

    /**
     * Nastavi ip adresu na rozhrani specifikovane v predchozim stavu cisco (promenna aktualni)
     * prikaz ip musi mit 3 argumenty, jina chybova hlaska.
     */
    private void ipaddress() {
        //ip address 192.168.2.129 255.255.255.128

        if ((slova.size() != 4) || (!kontrola("address", slova.get(1)))) {
//        if ((slova.size() != 4) || (!slova.get(1).equals("address"))) {
            if (nepokracovat) {
                kon.posliRadek("% Ambiguous command:  \"" + radek + "\"");
            } else {
                incompleteCommand();
            }
            return;
        }

        try {
            aktualni.ip = new IpAdresa(slova.get(2), slova.get(3));
        } catch (ZakazanaIpAdresaException e) {
            kon.posliRadek("Not a valid host address - " + slova.get(2));
        } catch (Exception e) {
            invalidInputDetected();
        }

    }
}
