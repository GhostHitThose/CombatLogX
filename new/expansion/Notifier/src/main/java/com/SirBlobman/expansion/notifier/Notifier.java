package com.SirBlobman.expansion.notifier;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.SirBlobman.combatlogx.event.PlayerCombatTimerChangeEvent;
import com.SirBlobman.combatlogx.event.PlayerUntagEvent;
import com.SirBlobman.combatlogx.expansion.CLXExpansion;
import com.SirBlobman.combatlogx.utility.PluginUtil;
import com.SirBlobman.combatlogx.utility.SchedulerUtil;
import com.SirBlobman.expansion.notifier.config.ConfigNotifier;
import com.SirBlobman.expansion.notifier.utility.ActionBarUtil;
import com.SirBlobman.expansion.notifier.utility.BossBarUtil;
import com.SirBlobman.expansion.notifier.utility.ScoreboardUtil;

import be.maximvdw.featherboard.api.FeatherBoardAPI;

public class Notifier implements CLXExpansion, Listener {
	public String getUnlocalizedName() {return "Notifier";}
	public String getVersion() {return "13.2";}
	
	public static File FOLDER;
	
	@Override
	public void enable() {
		FOLDER = getDataFolder();
		ConfigNotifier.load();
		PluginUtil.regEvents(this);
	}
	
	@Override
	public void disable() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			BossBarUtil.removeBossBar(player, true); 
			ActionBarUtil.removeActionBar(player);
		});
	}
	
	@Override
	public void onConfigReload() {
		ConfigNotifier.load();
	}
	
	@EventHandler
	public void onTimerChange(PlayerCombatTimerChangeEvent e) {
		Player player = e.getPlayer();
		
		if(ConfigNotifier.BOSS_BAR_ENABLED) BossBarUtil.updateBossBar(player);
		if(ConfigNotifier.ACTION_BAR_ENABLED) ActionBarUtil.updateActionBar(player);
		if(ConfigNotifier.SCORE_BOARD_ENABLED) {
			if(ConfigNotifier.SCORE_BOARD_USE_FEATHERBOARD) {
				FeatherBoardAPI.showScoreboard(player, ConfigNotifier.SCORE_BOARD_FEATHERBOARD_NAME);
			} else ScoreboardUtil.updateScoreBoard(player);
		}
	}
	
	@EventHandler
	public void onUntag(PlayerUntagEvent e) {
		Player player = e.getPlayer();
		
		if(ConfigNotifier.BOSS_BAR_ENABLED) BossBarUtil.removeBossBar(player, false);
		if(ConfigNotifier.ACTION_BAR_ENABLED) ActionBarUtil.removeActionBar(player);
		if(ConfigNotifier.SCORE_BOARD_ENABLED) {
			if(ConfigNotifier.SCORE_BOARD_USE_FEATHERBOARD) {
				SchedulerUtil.runLater(20L, () -> {
					FeatherBoardAPI.removeScoreboardOverride(player, "combatlogx"); 
				});
			} else ScoreboardUtil.removeScoreBoard(player);
		}
	}
}