package me.funky.praxi.adapter.core.impl;

import me.funky.praxi.adapter.core.Core;
import me.zatrex.core.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Nebula implements Core {

    @Override
    public String getName(UUID uuid) {
        Profile profile = new Profile(uuid);
        return profile.getActiveGrant().getRank().getName();
    }

    @Override
    public String getPrefix(UUID uuid) {
        Profile profile = new Profile(uuid);
        return profile.getActiveGrant().getRank().getPrefix();
    }

    @Override
    public String getSuffix(UUID uuid) {
        Profile profile = new Profile(uuid);
        return profile.getActiveGrant().getRank().getSuffix();
    }

    @Override
    public ChatColor getColor(UUID uuid) {
        Profile profile = new Profile(uuid);
        if (profile.getActiveGrant().getRank().getColor() == "&4") {
            return ChatColor.DARK_RED;
	    } else if (profile.getActiveGrant().getRank().getColor() == "&6") {
            return ChatColor.GOLD;
        } else if (profile.getActiveGrant().getRank().getColor() == "&c") {
            return ChatColor.RED;
        }
        return ChatColor.GREEN;
    }

    @Override
    public String getColoredName(UUID uuid) {
        Profile profile = new Profile(uuid);
        return profile.getActiveGrant().getRank().getColor() + Bukkit.getPlayer(uuid).getName();
    }

    @Override
    public String getRealName(Player player) {
        UUID uuid = player.getUniqueId();
        Profile profile = new Profile(uuid);
        return Bukkit.getPlayer(uuid).getName();
    }

    @Override
    public String getTag(Player player) {
        UUID uuid = player.getUniqueId();
        Profile profile = new Profile(uuid);
        return profile.getTag().getPrefix();
    }

    @Override
    public int getWeight(UUID uuid) {
        Profile profile = new Profile(uuid);
        return profile.getActiveGrant().getRank().getWeight();
    }
}
