package psimulator.userInterface.SimulatorEditor.DrawPanel.Support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Martin
 */
public class Validator {

    public static final String IP_WITH_MASK_PATTERN =
            "^((([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])/"
            + "([0-9]|[1-2][0-9]|3[0-2]))|)$";
    
    public static final String NAME_PATTERN =
            "^(\\w{0,15})$";
    
    public static final String MAC_PATTERN = 
            "^([0-9A-F][0-9A-F]-){5}([0-9A-F][0-9A-F])$";
    
    public static final String DELAY_PATTERN = 
            "^((0)|([1-9]{1}[0-9]{0,4}))$";

    public static boolean validateIpAddress(String address) {

        // check the IP address
        Pattern pattern = Pattern.compile(IP_WITH_MASK_PATTERN);
        Matcher matcher = pattern.matcher(address);

        // if valid
        if (matcher.matches()) {
            return true;
        }
        // wrong
        return false;
    }
    
    public static boolean validateMacAddress(String address){
        // check the IP address
        Pattern pattern = Pattern.compile(MAC_PATTERN);
        Matcher matcher = pattern.matcher(address);

        // if valid
        if (matcher.matches()) {
            return true;
        }
        // wrong
        return false;
    }
}
