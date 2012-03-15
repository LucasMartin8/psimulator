/*
 * Erstellt am 6.3.2012.
 */
package utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomas Pitrinec
 */
public class Util {

	public static final int deviceNameAlign = 8;

	public static boolean availablePort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/*
					 * should not be thrown
					 */
				}
			}
		}

		return false;
	}


		/**
		 * Klasicky printStackTrace hazi do stringu. Prejato z http://www.rgagnon.com/javadetails/java-0029.html.
		 *
		 * @param e
		 * @return
		 */


	public static String stackToString(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "stack trace ---\r\n" + sw.toString()
					+ "---------------\r\n";
		} catch (Exception e2) {
			return "Error in stackToString";
		}
	}

	/**
	 * Rozlozi cislo na mocniny dvojky. Pouzivam pri vypisovani navratovyho kodu.
	 *
	 * @param c
	 * @return
	 */
	public static String rozlozNaMocniny2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (1 << i);
				} else {
					vratit += " + " + (1 << i);
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "0";
		}
		return vratit;
	}

	public static String rozlozNaLogaritmy2(int c) {
		String vratit = "";
		for (int i = 0; i < 31; i++) {
			if ((c & (1 << i)) != 0) {
				if (vratit.equals("")) {
					vratit += (log2(1 << i));
				} else {
					vratit += ", " + (log2(1 << i));
				}
			}
		}
		if (vratit.equals("")) {
			vratit = "Zadny chybovy kod nebyl zadan.";
		}
		return vratit;
	}

	private static int log2(int num) {
		return (int) (Math.log(num) / Math.log(2));
	}

	public static int md(int c) {
		return (1 << c);
	}

	/**
	 * Zaokrouhluje na tri desetinna mista.
	 *
	 * @param d
	 * @return
	 */
	public static double zaokrouhli(double d) {
		return ((double) Math.round(d * 1000)) / 1000;
	}

	public static boolean jeInteger(String ret) {
		try {
			int a = Integer.parseInt(ret);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Uspi aktualni vlakno na pocet ms.
	 *
	 * @param ms
	 */
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Dorovna zadanej String mezerama na zadanou dylku. Kdyz je String delsi nez zadana dylka, tak nic neudela a String
	 * vrati nezmenenej. Protoze String se nikdy nemeni, ale vzdy se vytvori novej, se zadavany, Stringem se nic
	 * nestane.
	 *
	 * @param ret
	 * @param dylka
	 * @return
	 */
	public static String zarovnej(String ret, int dylka) {
		int dorovnat = dylka - ret.length();
		for (int i = 0; i < dorovnat; i++) {
			ret = ret + " ";
		}
		return ret;
	}

	/**
	 * Zjistuje, zda dany retezec zacina cislem. Nesmi byt static, jinak to hazi java.lang.IncompatibleClassChangeError:
	 * Expecting non-static method
	 *
	 * @param s
	 * @return
	 */
	public static boolean zacinaCislem(String s) {
		if (s.length() == 0) {
			return false;
		}

		if (Character.isDigit(s.charAt(0))) {
			return true;
		} else {
			return false;
		}
	}

	public static String threadName() {
		return Thread.currentThread().getName();
	}
}