package me.funky.praxi.adapter.core.impl;

import me.funky.praxi.adapter.core.Core;
import me.zowpy.core.api.CoreAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Volcano implements Core {

    @Override
    public String getName(UUID uuid) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getName();
    }

    @Override
    public String getPrefix(UUID uuid) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getPrefix();
    }

    @Override
    public String getSuffix(UUID uuid) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getSuffix();
    }

    @Override
    public ChatColor getColor(UUID uuid) {
		return ChatColor.valueOf(CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getColor().toUpperCase());
    }

    @Override
    public String getColoredName(UUID uuid) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getColor() + CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getName();
    }

    @Override
    public String getTag(Player player) {
        return null;
    }

    @Override
    public String getRealName(Player player) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(player.getUniqueId()).getName();
    }

    @Override
    public int getWeight(UUID uuid) {
        return CoreAPI.getInstance().getProfileManager().getByUUID(uuid).getRank().getWeight();
    }
}
