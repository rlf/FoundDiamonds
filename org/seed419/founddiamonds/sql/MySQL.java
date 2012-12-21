package org.seed419.founddiamonds.sql;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.FoundDiamonds;
import org.seed419.founddiamonds.file.Config;

import java.sql.*;

/*
Copyright 2011-2012 Blake Bartenbach

This file is part of FoundDiamonds.

FoundDiamonds is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FoundDiamonds is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FoundDiamonds.  If not, see <http://www.gnu.org/licenses/>.
*/

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

    public boolean blockWasPlaced(Location loc) {
        if (isConnected()) {
            String query = "SELECT * FROM " + prefix + "_placed WHERE world=? AND x=? AND y=? AND z=?";
            try {
                PreparedStatement ps = getPreparedStatement(query, loc);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
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

    public void clearPlaced(CommandSender sender) {
        if (isConnected()) {
            String delete = "DROP TABLE " + prefix + "_placed";
            writeToSQL(delete);
            sender.sendMessage(ChatColor.AQUA + "Placed blocks table dropped from SQL.");
            createTables();
        } else {
            getConnection();
        }
    }
}
