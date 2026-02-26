package me.hexett.staffUtilsPlus.utils;

import org.bukkit.ChatColor;

/**
 * Utility class for handling color codes and text formatting in Minecraft.
 * Supports both legacy color codes and hex color codes.
 * 
 * @author Hexett
 */
public class ColorUtils {
    
    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    /**
     * Translate color codes in a text string.
     * Supports both legacy color codes (&a, &b, etc.) and hex color codes (#FF0000).
     * 
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    public static String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));
        StringBuilder finalText = new StringBuilder();
        
        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                i++;
                if (i < texts.length && texts[i].charAt(0) == '#') {
                    // Handle hex color codes
                    if (texts[i].length() >= 7) {
                        finalText.append(net.md_5.bungee.api.ChatColor.of(texts[i].substring(0, 7)));
                        finalText.append(texts[i].substring(7));
                    } else {
                        finalText.append(texts[i]);
                    }
                } else {
                    // Handle legacy color codes
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }
        
        return finalText.toString();
    }
}
