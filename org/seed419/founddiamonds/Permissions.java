package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Prefix;

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

public class Permissions {


    private FoundDiamonds fd;


    public Permissions(FoundDiamonds fd) {
        this.fd = fd;
    }

    public boolean hasPerm(CommandSender sender, String permission) {
        return (sender.hasPermission(permission) || (fd.getConfig().getBoolean(Config.opsAsFDAdmin) && sender.isOp()));
    }

    public boolean hasAnyMenuPerm(CommandSender sender) {
        return (hasPerm(sender, "fd.manage.config") || hasPerm(sender, "fd.manage.reload")
                || hasPerm(sender, "fd.manage.toggle") || hasPerm(sender, "fd.manage.admin.add") || hasPerm(sender, "fd.manage.admin.remove")
                || hasPerm(sender, "fd.manage.world") || hasPerm(sender, "fd.manage.admin.list") || hasPerm(sender, "fd.manage.broadcast.add")
                || hasPerm(sender, "fd.manage.broadcast.remove") || hasPerm(sender, "fd.manage.broadcast.list") || hasPerm(sender, "fd.manage.light.add")
                || hasPerm(sender, "fd.manage.light.list") || hasPerm(sender, "fd.manage.light.remove") || hasPerm(sender, "fd.trap"));
    }

    public boolean hasBroadcastPerm(CommandSender sender) {
        return hasPerm(sender, "fd.broadcast");
    }

    public boolean hasMonitorPerm(CommandSender sender) {
        return hasPerm(sender, "fd.monitor");
    }

    public boolean hasAdminManagementPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.admin.add") || hasPerm(sender, "fd.manage.admin.remove")
                || hasPerm(sender, "fd.manage.admin.list");
    }

    public boolean hasBroadcastManagementPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.broadcast.add") || hasPerm(sender, "fd.broadcast.remove")
                || hasPerm(sender, "fd.broadcast.list");
    }

    public boolean hasLightManagementPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.light.add") || hasPerm(sender, "fd.manage.light.remove")
                || hasPerm(sender, "fd.manage.light.list");
    }

    public boolean hasReloadPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.reload");
    }

    public boolean hasTogglePerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.toggle");
    }

    public boolean hasConfigPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.config");
    }

    public boolean hasTrapPerm(CommandSender sender) {
        return hasPerm(sender, "fd.trap");
    }

    public boolean hasWorldManagementPerm(CommandSender sender) {
        return hasPerm(sender, "fd.manage.world");
    }

    public boolean hasAdminMessagePerm(CommandSender sender) {
        return hasPerm(sender, "fd.admin");
    }

    public void sendPermissionsMessage(CommandSender sender) {
        sender.sendMessage(Prefix.getChatPrefix() + ChatColor.RED + " You don't have permission to do that.");
        fd.getLog().warning(sender.getName() + " was denied access to a command.");
    }

}