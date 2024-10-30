package me.funky.praxi.commands.user.match;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.funky.praxi.queue.menu.BotFightMenu;
import me.funky.praxi.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("bot")
@Description("Open bot fight menu.")
public class BotCommand extends BaseCommand {

    @Default
    public void open(Player player) {
        new BotFightMenu().openMenu(player);
    }
}