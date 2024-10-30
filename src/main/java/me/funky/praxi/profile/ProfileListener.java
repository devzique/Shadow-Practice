package me.funky.praxi.profile;

import com.lunarclient.apollo.Apollo;
import me.funky.praxi.Locale;
import me.funky.praxi.Praxi;
import me.funky.praxi.match.MatchState;
import me.funky.praxi.adapter.core.CoreManager;
import me.funky.praxi.adapter.lunar.*;
import me.funky.praxi.essentials.event.SpawnTeleportEvent;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.profile.hotbar.HotbarItem;
import me.funky.praxi.profile.visibility.VisibilityLogic;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileListener implements Listener {

    private final RichPresence richPresence = Praxi.get().getRichPresence();

	@EventHandler(ignoreCancelled = true)
	public void onSpawnTeleportEvent(SpawnTeleportEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (!profile.isBusy() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			Hotbar.giveHotbarItems(event.getPlayer());
			PlayerUtil.reset(event.getPlayer(), false);
			Player player = event.getPlayer();
			player.getActivePotionEffects().clear();
		}
	}

	@EventHandler
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (profile.getState() != ProfileState.FIGHTING) {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (profile.getState() != ProfileState.FIGHTING) {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (profile.getState() == ProfileState.LOBBY) {
			event.setCancelled(true);
		}
	}

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
	    event.setFormat(CC.translate(profile.getDivision().getDisplayName() + " &7┃ " + getColoredName(event.getPlayer()) + "&f: " + event.getMessage()));
    }

	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

			if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
				event.setCancelled(true);

				if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
					Praxi.get().getEssentials().teleportToSpawn((Player) event.getEntity());
				}
			}
		}
	}

	@EventHandler
    public void soilChangePlayer(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
            event.setCancelled(true);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			Player player = event.getPlayer();

			HotbarItem hotbarItem = Hotbar.fromItemStack(event.getItem());

			if (hotbarItem != null) {
				if (hotbarItem.getCommand() != null) {
					event.setCancelled(true);
					player.chat("/" + hotbarItem.getCommand());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		Profile profile = new Profile(event.getUniqueId());

		try {
			profile.load();
		} catch (Exception e) {
			e.printStackTrace();
			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(ChatColor.RED + "Failed to load your profile. Try again later.");
			return;
		}

		Profile.getProfiles().put(event.getUniqueId(), profile);
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		event.setJoinMessage(null);
        Player player = event.getPlayer();
        Profile profile = new Profile(player.getUniqueId());

		for (String line : Praxi.get().getMainConfig().getStringList("JOIN_MESSAGES")) {
			player.sendMessage(CC.translate(line));
		}

        if (Praxi.get().isLunar()) {
             if (Apollo.getPlayerManager().hasSupport(player.getUniqueId())) {
                 this.richPresence.overrideServerRichPresence(player);
             }
        }

		new BukkitRunnable() {
			@Override
			public void run() {
				Hotbar.giveHotbarItems(player);
				Praxi.get().getEssentials().teleportToSpawn(player);
                player.setPlayerTime(profile.getOptions().time().getTime(), false);

				for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
					VisibilityLogic.handle(player, otherPlayer);
					VisibilityLogic.handle(otherPlayer, player);
				}
			}
		}.runTaskLater(Praxi.get(), 4L);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (Apollo.getPlayerManager().hasSupport(event.getPlayer().getUniqueId()) && Praxi.get().isLunar()) {
            this.richPresence.resetServerRichPresence(event.getPlayer());
        }

        if (!profile.getFollowers().isEmpty()) {
            for (UUID playerUUID : profile.getFollowers()) {
                Bukkit.getPlayer(playerUUID).sendMessage(Locale.FOLLOWED_LEFT.format(Bukkit.getPlayer(playerUUID), event.getPlayer().getName()));
            }
        }

        if (!profile.getFollowing().isEmpty()) {
            List<UUID> followingCopy = new ArrayList<>(profile.getFollowing());

            for (UUID playerUUID : followingCopy) {
                Profile followerProfile = Profile.getByUuid(playerUUID);
                followerProfile.getFollowers().remove(event.getPlayer().getUniqueId());

                profile.getFollowing().remove(playerUUID);
            }
        }

		new BukkitRunnable() {
			@Override
			public void run() {
				profile.save();
			}
		}.runTaskAsynchronously(Praxi.get());

        if (profile.getMatch() != null) {
            if (profile.getMatch().getState().equals(MatchState.PLAYING_ROUND)
                    || profile.getMatch().getState().equals(MatchState.ENDING_MATCH)
                    || profile.getMatch().getState().equals(MatchState.STARTING_ROUND)) {
                profile.getMatch().broadcast("&c" + event.getPlayer().getName() + " &fDisconnected");
            }

            profile.getMatch().onDeath(event.getPlayer());
        }

		if (profile.getRematchData() != null) {
			profile.getRematchData().validate();
		}
        Profile.getProfiles().remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerKickEvent(PlayerKickEvent event) {
		if (event.getReason() != null) {
			if (event.getReason().contains("Flying is not enabled")) {
				event.setCancelled(true);
			}
		}
	}

    private String getColoredName(Player player) {
		return CoreManager.getInstance().getCore().getColoredName(player.getUniqueId());
	}
}