/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seed419.founddiamonds;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.entity.Player;

/**
 *
 * @author proxa
 */
public class PlaytimeManager {
    
    
    private static DecimalFormat dForm = new DecimalFormat("#.###");
    private static HashMap<Player,Date> playtime = new HashMap<Player,Date>();
    
    
    public static void insertEntry(Player player, Date date) {
        System.out.println("PlaytimeManager Entry added: " + player.getName() + ", " + date);
        playtime.put(player, date);
    }

    public static void calculatePlaytime(Player player, Date date) {
        BigDecimal test = new BigDecimal((date.getTime() - playtime.get(player).getTime()) / 1000.0 / 60.0 / 60.0);
        String testForm = dForm.format(test);
        System.out.println("Total hours of playtime: " + test);
        System.out.println("TestFormat: " + testForm);
    }
    
}
