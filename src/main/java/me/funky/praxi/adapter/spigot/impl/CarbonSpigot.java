package me.funky.praxi.adapter.spigot.impl;

import me.funky.praxi.adapter.spigot.Spigot;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.refinedev.spigot.api.knockback.KnockbackAPI;

public class CarbonSpigot implements Spigot {
    @Override
    public void setKnockback(Player player, String kb) {
        KnockbackAPI.getInstance().setPlayerProfile(player, KnockbackAPI.getInstance().getProfile(kb));
    }
}
