package com.SirBlobman.towny;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.SirBlobman.combatlogx.Combat;
import com.SirBlobman.combatlogx.config.ConfigLang;
import com.SirBlobman.combatlogx.config.NoEntryMode;
import com.SirBlobman.combatlogx.expansion.CLXExpansion;
import com.SirBlobman.combatlogx.utility.Util;
import com.SirBlobman.towny.config.ConfigTowny;

public class CompatTowny implements CLXExpansion, Listener {
    public static File FOLDER;

    @Override
    public void enable() {
        if (Util.PM.isPluginEnabled("Towny")) {
            FOLDER = getDataFolder();
            ConfigTowny.load();
            Util.regEvents(this);
        } else {
            String error = "Towny is not installed. This expansion is useless!";
            print(error);
        }
    }

    public String getUnlocalizedName() {return "CompatTowny";}
    public String getName() {return "Towny Compatibility";}
    public String getVersion() {return "3";}

    @EventHandler
    public void move(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        checkEvent(p, e, from, to);
    }

    @EventHandler
    public void tp(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        checkEvent(p, e, from, to);
    }

    private void checkEvent(Player p, Cancellable e, Location from, Location to) {
        if(e.isCancelled()) return;
        else {
            if(ConfigTowny.OPTION_NO_SAFEZONE_ENTRY && Combat.isInCombat(p)) {
                boolean pvp = TownyUtil.pvp(to);
                if(!pvp) {
                    if(e instanceof PlayerTeleportEvent) e.setCancelled(true);
                    else {
                        if(p.isInsideVehicle()) p.leaveVehicle();
                        String mode = ConfigTowny.OPTION_NO_SAFEZONE_ENTRY_MODE;
                        NoEntryMode nem = NoEntryMode.valueOf(mode);
                        if(nem == null) nem = NoEntryMode.CANCEL;

                        if(nem == NoEntryMode.CANCEL) {
                            e.setCancelled(true);
                        } else if(nem == NoEntryMode.TELEPORT) {
                            LivingEntity enemy = Combat.getEnemy(p);
                            if(enemy != null && !enemy.equals(p)) {
                                Location loc = enemy.getLocation();
                                p.teleport(loc);
                            } else p.teleport(from);
                        } else if(nem == NoEntryMode.KILL) {
                            p.setHealth(0.0D);
                        } else if(nem == NoEntryMode.KNOCKBACK) {
                            Vector vf = from.toVector();
                            Vector vt = to.toVector();
                            Vector v = vf.subtract(vt);
                            v = v.normalize();
                            v = v.setY(0);
                            v = v.multiply(ConfigTowny.OPTION_NO_SAFEZONE_ENTRY_STRENGTH);
                            p.setVelocity(v);
                        }

                        String error = ConfigLang.MESSAGE_NO_ENTRY;
                        Util.sendMessage(p, error);
                    }
                }
            } else return;
        }
    }
}