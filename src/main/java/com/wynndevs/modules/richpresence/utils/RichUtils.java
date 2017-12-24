package com.wynndevs.modules.richpresence.utils;

import com.wynndevs.ConfigValues;
import com.wynndevs.modules.richpresence.WynnRichPresence;
import com.wynndevs.modules.richpresence.profiles.LocationProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by HeyZeer0 on 04/12/2017.
 * Copyright © HeyZeer0 - 2016
 */
public class RichUtils {

    public static ArrayList<LocationProfile> locations = new ArrayList<>();
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");

    /**
     * Send a request to WynnAPI and save all regions at {@link com.wynndevs.modules.richpresence.utils.RichUtils#locations}
     */
    public static void updateRegions() {
        new Thread(() -> {
            try{
                URLConnection st = new URL("https://api.wynncraft.com/public_api.php?action=territoryList").openConnection();
                st.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

                JSONObject main = new JSONObject(IOUtils.toString(st.getInputStream())).getJSONObject("territories");

                for(String key : main.keySet()) {
                    if(main.getJSONObject(key).has("location")) {
                        JSONObject loc = main.getJSONObject(key).getJSONObject("location");
                        locations.add(new LocationProfile(key, loc.getInt("startX"), loc.getInt("startY"), loc.getInt("endX"), loc.getInt("endY")));
                    }
                }

                locations.add(new LocationProfile("Rodoroc", 1009, -5231, 1263, -5057));

            }catch (Exception ex) {
                WynnRichPresence.logger.warn("Error captured while trying to connect to Wynncraft location api", ex);}

        }).start();
    }

    /**
     * Removes all color codes from a string
     *
     * @param input
     *        Input string
     *
     * @return input string without colored chars
     */
    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Returns a cutted string after x characters
     *
     * @param x
     *        Original String
     * @param amount
     *        The max string char amount
     *
     * @return Original string cutted after x characters
     */
    public static String removeAfterChar(String x, int amount) {
        String toReturn = x;
        if(toReturn.length() > amount) {
            toReturn = toReturn.substring(0, toReturn.length() - (toReturn.length() - amount));
            toReturn = toReturn + "...";
        }
        return toReturn;
    }

    /**
     * Just a simple method to short other ones
     * @return RichPresence largeImageText
     */
    public static String getPlayerInfo() {
        Minecraft mc = Minecraft.getMinecraft();
        return ConfigValues.wynnRichPresence.discordConfig.showNicknameAndClass ? mc.player.getName() + " | Level " + mc.player.experienceLevel + " " + getPlayerCurrentClass() : null;
    }

    /**
     * Just a simple way to get the current player class
     * @return Player Current Class
     */
    public static String getPlayerCurrentClass(){
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player.experienceLevel > 0) {
            try {
                ItemStack book = mc.player.inventory.getStackInSlot(7);
                if (book.hasDisplayName() && book.getDisplayName().contains("Quest Book")) {
                    for (int i=0;i<36;i++) {
                        try {
                            ItemStack ItemTest = mc.player.inventory.getStackInSlot(i);
                            NBTTagList Lore = ItemTest.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                            for (int j = 1; j < Lore.tagCount(); j++) {
                                String ClassTest = Lore.get(j).toString();
                                if (ClassTest.contains("Class Req:") && ClassTest.charAt(2) == 'a'){
                                    return ClassTest.substring(18,ClassTest.lastIndexOf('/'));
                                }
                            }
                        }
                        catch (Exception ignored){
                        }
                    }
                }
            }
            catch (Exception ignored) {
                return "";
            }
        }
        return "";
    }

}