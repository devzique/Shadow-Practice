package me.funky.praxi.match.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.NMS;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.Arena;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.match.Match;
import me.funky.praxi.match.MatchState;
import me.funky.praxi.bots.Bot;
import me.funky.praxi.match.participant.MatchGamePlayer;
import me.funky.praxi.party.Party;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.meta.ProfileKitData;
import me.funky.praxi.profile.meta.ProfileRematchData;
import me.funky.praxi.participant.GameParticipant;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.*;
import me.funky.praxi.util.elo.EloUtil;
import me.funky.praxi.util.config.BasicConfigurationFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
   BotMatch.java
   Author: Zonix
   Edited By: Zatrex
 */
@Getter
public class BotMatch extends Match {

	private GameParticipant<MatchGamePlayer> participantA;
	private @Setter GameParticipant<MatchGamePlayer> winningParticipant;
	private @Setter GameParticipant<MatchGamePlayer> losingParticipant;
	private HashMap<UUID, Bot> npcRegistry;

	private Bot.BotDifficulty difficulty;

	public BotMatch(Queue queue, Kit kit, Arena arena, boolean ranked, Bot.BotDifficulty difficulty, GameParticipant<MatchGamePlayer> participantA, boolean duel) {
		super(queue, kit, arena, ranked, duel);

		this.difficulty = difficulty;
		this.participantA = participantA;
		this.npcRegistry = new HashMap<>();
	}

	@Override
	public void setupPlayer(Player player) {
		super.setupPlayer(player);
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "PvPBot");

		// Teleport the player to their spawn point
		Location spawn = arena.getSpawnA();
		Location spawnBot = arena.getSpawnB();

		//if (spawn.getBlock().getType() == Material.AIR) player.teleport(spawn);
		//else player.teleport(spawn.add(0, 2, 0));
		player.teleport(spawn);

		npc.data().set("player-skin-name", (Object)"ClubSpigot");
		npc.spawn(spawnBot.add(0, 2, 0));

		Bot bot = new Bot();
		bot.setBotDifficulty(difficulty);
		bot.setKit(kit);
		bot.setArena(arena);
		bot.setDestroyed(false);
		bot.setNpc(npc);
		bot.startMechanics(Collections.singletonList(player.getUniqueId()), difficulty);
		player.showPlayer(bot.getBukkitEntity());
		bot.getBukkitEntity().showPlayer(player);
		this.npcRegistry.put(player.getUniqueId(), bot);
        npc.getOrAddTrait(LookClose.class).setRange(10);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc.isSpawned() && player.isOnline()) {
                    npc.getNavigator().setTarget(player.getLocation());
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Praxi.get(), 0L, 20L);
       //}
	}

	@Override
	public void end() {
		if (participantA.getPlayers().size() == 1 && npcRegistry.size() == 1) {
			for (GameParticipant<MatchGamePlayer> gameParticipant : getParticipants()) {
				for (MatchGamePlayer gamePlayer : gameParticipant.getPlayers()) {
					if (!gamePlayer.isDisconnected()) {
						Profile profile = Profile.getByUuid(gamePlayer.getUuid());

						if (profile.getParty() == null) {
							if (gamePlayer.getPlayer() == null) {
								super.end();
								Bot bot = this.npcRegistry.get(gamePlayer.getUuid());
								if (bot.getBotMechanics() != null) {
									bot.getBotMechanics().cancel();
								}
								this.npcRegistry.remove(gamePlayer.getUuid());
								bot.destroy();
								return;
							}
						}
					}
				}
			}
		}

		super.end();
	}

	@Override
	public boolean canEndMatch() {
		return true;
	}

	@Override
	public boolean canStartRound() {
		return kit.getGameRules().isBridge();
	}

	@Override
	public void onRoundEnd() {
		super.onRoundEnd();
	}

	@Override
	public boolean canEndRound() {
		return participantA.isAllDead();
	}

	@Override
	public boolean isOnSameTeam(Player first, Player second) {
		return false;
	}

	@Override
	public List<GameParticipant<MatchGamePlayer>> getParticipants() {
		return Arrays.asList(participantA);
	}

	@Override
	public ChatColor getRelationColor(Player viewer, Player target) {
		return ChatColor.YELLOW;
	}

    @Override
	public List<BaseComponent[]> generateEndComponents() {
		List<BaseComponent[]> componentsList = new ArrayList<>();

		for (String line : Locale.MATCH_END_DETAILS.formatLines()) {
			if (line.equalsIgnoreCase("%INVENTORIES%")) {

				BaseComponent[] winners = generateInventoriesComponents(
						Locale.MATCH_END_WINNER_INVENTORY.format(participantA.getPlayers().size() == 1 ? "" : "s"), winningParticipant);

				BaseComponent[] losers = generateInventoriesComponents(
						Locale.MATCH_END_LOSER_INVENTORY.format(npcRegistry.size() > 1 ? "s" : ""), losingParticipant);

				componentsList.add(winners); // &a&l[Replay]
				componentsList.add(losers);

				continue;
			}

			if (line.equalsIgnoreCase("%ELO_CHANGES%")) {
				continue;
			}

			componentsList.add(new ChatComponentBuilder("").parse(line).create());
		}

		return componentsList;
	}

}
