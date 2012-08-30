package org.seed419.founddiamonds;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.seed419.founddiamonds.file.Config;
import org.seed419.founddiamonds.util.Prefix;

/**
 * Attribute Only (Public) License
 * Version 0.a3, July 11, 2011
 * <p/>
 * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
 * <p/>
 * Anyone is allowed to copy and distribute verbatim or modified
 * copies of this license document and altering is allowed as long
 * as you attribute the author(s) of this license document / files.
 * <p/>
 * ATTRIBUTE ONLY PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 * <p/>
 * 1. Attribute anyone attached to the license document.
 * Do not remove pre-existing attributes.
 * <p/>
 * Plausible attribution methods:
 * 1. Through comment blocks.
 * 2. Referencing on a site, wiki, or about page.
 * <p/>
 * 2. Do whatever you want as long as you don't invalidate 1.
 *
 * @license AOL v.a3 <http://aol.nexua.org>
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
