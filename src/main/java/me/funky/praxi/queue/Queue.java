package me.funky.praxi.queue;

import lombok.Getter;
import me.funky.praxi.Locale;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.profile.ProfileState;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.profile.hotbar.Hotbar;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ReplaceUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

public class Queue {

	@Getter private static List<Queue> queues = new ArrayList<>();

	@Getter private final UUID uuid = UUID.randomUUID();
	@Getter private final Kit kit;
	@Getter private final boolean ranked;
	@Getter private final LinkedList<QueueProfile> players = new LinkedList<>();
    @Getter private int queuing;

	public Queue(Kit kit, boolean ranked) {
		this.kit = kit;
		this.ranked = ranked;
        this.queuing = 0;

		queues.add(this);
	}

	public String getQueueName() {
		return (ranked ? "Ranked" : "Unranked") + " " + kit.getName();
	}

	public void addPlayer(Player player, int elo) {
		QueueProfile queueProfile = new QueueProfile(this, player.getUniqueId());
		queueProfile.setElo(elo);

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setQueueProfile(queueProfile);
		profile.setState(ProfileState.QUEUEING);

		players.add(queueProfile);

		Hotbar.giveHotbarItems(player);

		if (ranked) {
			if (!profile.isRankedBan()) {
			 player.sendMessage(Locale.QUEUE_JOIN_RANKED.format(kit.getName(), elo));
	    } else {
				player.sendMessage(CC.translate("&cYou are permanently banned from ranked."));
				player.sendMessage(CC.translate("&cReason: &f" + profile.getRankedBanReason()));
				player.sendMessage(CC.translate("&cBan ID: &f" + profile.getRankedBanID()));
				removePlayer(queueProfile);
			}
		} else {
			player.sendMessage(Locale.QUEUE_JOIN_UNRANKED.format(kit.getName()));
		}
	}

	public void removePlayer(QueueProfile queueProfile) {
		players.remove(queueProfile);

		Profile profile = Profile.getByUuid(queueProfile.getPlayerUuid());
		profile.setQueueProfile(null);
		profile.setState(ProfileState.LOBBY);

		Player player = Bukkit.getPlayer(queueProfile.getPlayerUuid());

		if (player != null) {
			Hotbar.giveHotbarItems(player);

			if (ranked) {
				player.sendMessage(Locale.QUEUE_LEAVE_RANKED.format(kit.getName()));
			} else {
				player.sendMessage(Locale.QUEUE_LEAVE_UNRANKED.format(kit.getName()));
			}
		}

	}

	public static Queue getByUuid(UUID uuid) {
		for (Queue queue : queues) {
			if (queue.getUuid().equals(uuid)) {
				return queue;
			}
		}

		return null;
	}

    public void addQueue() {
        queuing += 1;
    }

    public void removeQueue() {
        queuing -= 1;
    }
}
