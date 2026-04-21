package cn.lunadeer.mc.dominionSAR.utils;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Misc {

    private static Boolean isPaper = null;

    /**
     * Checks if the server is running Paper.
     * <p>
     * This method attempts to load a specific class that is unique to Paper servers.
     * If the class is found, it indicates that the server is running Paper.
     * Otherwise, it returns false.
     *
     * @return true if the server is running Paper, false otherwise
     */
    public static boolean isPaper() {
        if (isPaper != null) return isPaper;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            isPaper = true;
            return true;
        } catch (ClassNotFoundException e) {
            isPaper = false;
            return false;
        }
    }

    /**
     * Formats a string by replacing placeholders with the provided arguments.
     * <p>
     * Each placeholder in the format `{index}` within the string is replaced
     * with the corresponding argument from the `args` array. If an argument is `null`,
     * it is replaced with a default string indicating the null value.
     *
     * @param str  the string containing placeholders to format
     * @param args the arguments to replace placeholders in the string
     * @return the formatted string
     */
    public static String formatString(String str, Object... args) {
        String formatStr = str;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = "[null for formatString (args[" + i + "])]";
            }
            formatStr = formatStr.replace("{" + i + "}", args[i].toString());
        }
        return formatStr;
    }

    /**
     * Formats a list of strings by replacing placeholders with the provided arguments.
     * <p>
     * Each placeholder in the format `{index}` within the strings in the list is replaced
     * with the corresponding argument from the `args` array. If an argument is `null`,
     * it is replaced with a default string indicating the null value.
     *
     * @param list the list of strings to format
     * @param args the arguments to replace placeholders in the strings
     * @return a new list of formatted strings
     */
    public static List<String> formatStringList(List<String> list, Object... args) {
        List<String> formattedList = new ArrayList<>(list);
        for (int i = 0; i < args.length; i++) {
            for (int j = 0; j < list.size(); j++) {
                formattedList.set(j, formattedList.get(j).replace("{" + i + "}", args[i].toString()));
            }
        }
        return formattedList;
    }

}
