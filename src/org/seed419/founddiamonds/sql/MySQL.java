package org.seed419.founddiamonds.sql;

import java.sql.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.seed419.founddiamonds.Config;
import org.seed419.founddiamonds.EventInformation;
import org.seed419.founddiamonds.FoundDiamonds;

/**
 *
 * @author proxa
 */
public class MySQL {


    private FoundDiamonds fd;
    private Connection connection;
    private final static Logger log = Logger.getLogger("FoundDiamonds");
    private String username;
    private String password;
    private String url;


    public MySQL(FoundDiamonds fd) {
        this.fd = fd;
    }

    public void getConnection() {
        if (!fd.getConfig().getBoolean(Config.mysqlEnabled)) {
            return;
        }
        String username = fd.getConfig().getString(Config.mysqlUsername);
        String password = fd.getConfig().getString(Config.mysqlPassword);
        String url = getMysqlUrl();
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            createTables();
            log.info(FoundDiamonds.getLoggerPrefix() + " MySQL backend connected");
        } catch (SQLException ex) {
            log.severe("Couldn't establish mysql connection.  Check your settings and verify "
                    + "that the database is actually running.");
            ex.printStackTrace();
        }
    }

    public String getMysqlUrl() {
        return "jdbc:mysql://" + fd.getConfig().getString(Config.mysqlUrl)
                + ":" + fd.getConfig().getString(Config.mysqlPort) + "/" +
                fd.getConfig().getString(Config.mysqlDatabase);
    }

    public void createTables() {
        writeToSQL("CREATE TABLE IF NOT EXISTS `" + fd.getConfig().getString(Config.mysqlPrefix)
            + "_blocks` (`player` varchar(16) NOT NULL,"
            + "`diamond` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`gold` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`lapis` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`iron` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`coal` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`redstone` int(32) unsigned NOT NULL DEFAULT '0',"
            + "`hours` int(32) unsigned NOT NULL DEFAULT '0',"
            + "PRIMARY KEY (`player`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
        //TODO vein table?  maybe?  what about silk touch...
//        writeToSQL("CREATE TABLE IF NOT EXISTS `" + fd.getConfig().getString(Config.mysqlPrefix)
//            + "_blocks` (`player` varchar(16) NOT NULL,"
//            + "`diamond` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`gold` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`lapis` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`iron` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`coal` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`redstone` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "`hours` int(32) unsigned NOT NULL DEFAULT '0',"
//            + "PRIMARY KEY (`player`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
    }

    public void updateUser(EventInformation ei) {
        if (isConnected()) {
            try {
                Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM fd_blocks WHERE player='" + ei.getPlayer().getName() + "'");
                String type = getBlockName(ei);
                if (rs.next()) {
                    int current = rs.getInt(type);
                    s = connection.createStatement();
                    s.executeUpdate("UPDATE fd_blocks SET " + type + "=" + (current+ei.getTotal()) + " WHERE player='" + ei.getPlayer().getName() + "'");
                } else {
                    writeToSQL("INSERT INTO fd_blocks (player," + type + ") VALUES('" + ei.getPlayer().getName() + "'," + ei.getTotal() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            getConnection();
        }
    }

    public boolean writeToSQL(String sql) {
        if (isConnected()) {
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.executeUpdate();
                return true;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        } else {
            getConnection();
            return true;
        }
    }

    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(3);
        } catch (SQLException e) {
            log.warning(FoundDiamonds.getLoggerPrefix() + " MySQL not connected.");
            e.printStackTrace();
            return false;
        }
    }


    public void handleTop(CommandSender sender, String ore) {
        if (isConnected()) {
            try {
                Statement s = connection.createStatement();
                ResultSet r = s.executeQuery("SELECT * FROM fd_blocks ORDER BY "+ore+" DESC LIMIT 10");
                sender.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " [Top "+ore+"]");
                int counter = 1;
                while (r.next()) {
                    sender.sendMessage(" " + counter + ". " + ChatColor.GREEN +  r.getInt(ore)
                            + " - " + ChatColor.WHITE +   r.getString("player"));
                    counter++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            getConnection();
        }
    }

    private String getBlockName(EventInformation ei) {
        switch (ei.getMaterial()) {
            case DIAMOND_ORE:
                return "diamond";
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                return "redstone";
            case LAPIS_ORE:
                return "lapis";
            case COAL_ORE:
                return "coal";
            case IRON_ORE:
                return "iron";
            case GOLD_ORE:
                return "gold";
            default:
                log.severe("Type doesn't exist");
                return null;
        }
    }

    public void printStats(Player player) {
        if (isConnected()) {
            try {
                Statement s = connection.createStatement();
                ResultSet r = s.executeQuery("SELECT * FROM fd_blocks WHERE player='" + player.getName() + "'");
                    player.sendMessage(FoundDiamonds.getPrefix() + ChatColor.AQUA + " [" + player.getName() + "'s Stats]");
                if (r.next()) {
                    player.sendMessage(ChatColor.RED + "Diamond: " + ChatColor.AQUA + r.getInt("diamond"));
                    player.sendMessage(ChatColor.RED + "Gold: " + ChatColor.GOLD + r.getInt("gold"));
                    player.sendMessage(ChatColor.RED + "Lapis: " + ChatColor.BLUE + r.getInt("lapis"));
                    player.sendMessage(ChatColor.RED + "Redstone: " + ChatColor.DARK_RED + r.getInt("redstone"));
                    player.sendMessage(ChatColor.RED + "Iron: " + ChatColor.GRAY + r.getInt("iron"));
                    player.sendMessage(ChatColor.RED + "Coal: " + ChatColor.DARK_GRAY + r.getInt("coal"));
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have any stats yet!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            getConnection();
        }
    }

}
