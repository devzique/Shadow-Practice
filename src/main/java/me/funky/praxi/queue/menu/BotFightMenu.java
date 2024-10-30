package me.funky.praxi.queue.menu;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.bots.Bot;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.impl.BasicTeamMatch;
import me.funky.praxi.match.impl.BotMatch;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class BotFightMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&8Select Difficulty";
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        for (Bot.BotDifficulty difficulty : Bot.BotDifficulty.values()) {
            buttons.put(difficulty.getSlot(), new DifficultyButton(difficulty));
        }
        return buttons;
    }

    @RequiredArgsConstructor
    private static class DifficultyButton extends Button {

        private final Bot.BotDifficulty difficulty;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .lore("")
                    .lore("&bDifficulty&f: " + difficulty)
                    .lore("&bReach&f: " + difficulty.getReach())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            handleTrainMapClick(player, difficulty);
        }
    }

    private static void handleTrainMapClick(final Player player, final Bot.BotDifficulty difficulty) {
        player.closeInventory();
        Match match;
        Kit kit = Kit.getByName("NoDebuff");
        Arena arena = Arena.getRandomArena(kit);

        MatchGamePlayer playerA = new MatchGamePlayer(player.getUniqueId(), Profile.getByUuid(player.getUniqueId()) + player.getName());
        GameParticipant<MatchGamePlayer> participantA = new GameParticipant<>(playerA);

        match = new BotMatch(null, kit, arena, false, difficulty, participantA, true); // set bot fight as a duel
        match.start();
    }

}