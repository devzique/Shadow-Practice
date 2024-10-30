package me.funky.praxi.adapter.core.impl;

import me.funky.praxi.adapter.core.Core;
import net.versedevelopment.core.api.player.GlobalPlayer;
import net.versedevelopment.core.api.player.PlayerData;
import net.versedevelopment.core.plugin.VerseCoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VerseCore implements Core {

    @Override
    public String getName(UUID uuid) {
        PlayerData data = VerseCoreAPI.INSTANCE.getPlayerData(uuid);
        return data == null ? "No Data" : data.getHighestRank().getName();
    }

    @Override
    public String getPrefix(UUID uuid) {
        PlayerData data = VerseCoreAPI.INSTANCE.getPlayerData(uuid);
        return data == null ? "No Data" : data.getHighestRank().getPrefix();
    }

    @Override
    public String getSuffix(UUID uuid) {
        PlayerData data = VerseCoreAPI.INSTANCE.getPlayerData(uuid);
        return data == null ? "No Data" : data.getHighestRank().getSuffix();
    }

    @Override
    public ChatColor getColor(UUID uuid) {
        PlayerData data = VerseCoreAPI.INSTANCE.getPlayerData(uuid);
        return data == null ? null : data.getHighestRank().getColor();
    }

    @Override
    public String getColoredName(UUID uuid) {
        PlayerData data = VerseCoreAPI.INSTANCE.getPlayerData(uuid);
        return data == null ? "No Data" : data.getHighestRank().getColor().toString();
    }

    @Override
    public String getRealName(Player player) {
		return VerseCoreAPI.INSTANCE.getRealName(player);
    }

    @Override
    public String getTag(Player player) {
        return VerseCoreAPI.INSTANCE.getTag(player.getUniqueId()).getFormat();
    }

    @Override
    public int getWeight(UUID uuid) {
        GlobalPlayer globalPlayer = VerseCoreAPI.INSTANCE.getGlobalPlayer(uuid);
        return globalPlayer == null ? 0 : globalPlayer.getRankWeight();
    }

}
