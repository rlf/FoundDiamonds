package org.seed419.founddiamonds.util;

import org.bukkit.ChatColor;

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

public class Prefix {


    private final static String chatPrefix = "[FD]";
    private final static String menuPrefix = ChatColor.BOLD + "[FD] " + ChatColor.RESET;
    private final static String adminPrefix = ChatColor.RED + "[FD]";
    private final static String debugPrefix = "[FD Debug] ";
    private final static String loggingPrefix = "[FoundDiamonds] ";


    public static String getChatPrefix() {
        return chatPrefix;
    }

    public static String getAdminPrefix() {
        return adminPrefix;
    }

    public static String getDebugPrefix() {
        return debugPrefix;
    }

    public static String getLoggingPrefix() {
        return loggingPrefix;
    }

    public static String getMenuPrefix() {
        return menuPrefix;
    }
}
