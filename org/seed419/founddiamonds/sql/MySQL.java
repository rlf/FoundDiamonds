package org.seed419.founddiamonds.sql;

import org.bukkit.Location;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;

import java.sql.*;

public class MySQL {


    private FoundDiamonds fd;
    private Connection connection;
    private String prefix;


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
        prefix = fd.getConfig().getString(Config.mysqlPrefix);
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            createTables();
            fd.getLog().info("MySQL backend connected");
        } catch (SQLException ex) {
            fd.getLog().severe("Couldn't establish mysql connection.  Check your settings and verify "
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
        writeToSQL("CREATE TABLE IF NOT EXISTS `" + prefix
                + "_placed` (`world` varchar(16) NOT NULL,"
                + "`x` int(32) signed NOT NULL DEFAULT '0',"
                + "`y` int(32) signed NOT NULL DEFAULT '0',"
                + "`z` int(32) signed NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`world`,`x`,`y`,`z`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
    }
/*
    public void updateUser(EventInformation ei) {
        if (isConnected()) {
            try {
                Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + prefix + "_blocks WHERE player='" + ei.getPlayer().getName() + "'");
                String type = getBlockName(ei);
                if (rs.next()) {
                    int current = rs.getInt(type);
                    s = connection.createStatement();
                    s.executeUpdate("UPDATE " + prefix + "_blocks SET " + type + "=" + (current+ei.getTotal()) + " WHERE player='" + ei.getPlayer().getName() + "'");
                } else {
                    writeToSQL("INSERT INTO " + prefix + "_blocks (player," + type + ") VALUES('" + ei.getPlayer().getName() + "'," + ei.getTotal() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            getConnection();
        }
    }*/

    public boolean blockWasPlaced(Location loc) {
        if (isConnected()) {
            String query = "SELECT * FROM " + prefix + "_placed WHERE world=? AND x=? AND y=? AND z=?";
            try {
                PreparedStatement ps = getPreparedStatement(query, loc);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("block was placed");
                    return true;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            getConnection();
        }
        return false;
    }

    public void removePlacedBlock(Location loc) {
        if (isConnected()) {
            try {
                String update = "DELETE FROM " + prefix + "_placed WHERE world=? AND x=? AND y=? AND z=?";
                PreparedStatement ps = getPreparedStatement(update, loc);
                ps.executeUpdate();
            } catch (SQLException ex) {
                fd.getLog().warning("Unable to remove block from placed database...");
                ex.printStackTrace();
            }
        }
    }

    public PreparedStatement getPreparedStatement(String string, Location loc) {
        try {
            PreparedStatement ps = connection.prepareStatement(string);
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            return ps;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        fd.getLog().warning("Unable to prepare statement.");
        return null;
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

    public void updatePlacedBlockinSQL(Location loc) {
        if (isConnected()) {
            String update = "INSERT INTO " + prefix + "_placed VALUES(?,?,?,?)";
            try {
                PreparedStatement ps = getPreparedStatement(update, loc);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            getConnection();
        }
    }

    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(3);
        } catch (SQLException e) {
            fd.getLog().warning("MySQL not connected.");
            e.printStackTrace();
            return false;
        }
    }
/*

    public void handleTop(CommandSender sender, String ore) {
        if (isConnected()) {
            try {
                Statement s = connection.createStatement();
                ResultSet r = s.executeQuery("SELECT * FROM " + prefix + "_blocks ORDER BY "+ore+" DESC LIMIT 10");
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
                ResultSet r = s.executeQuery("SELECT * FROM " + prefix + "_blocks WHERE player='" + player.getName() + "'");
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
    }*/
}
