package me.funky.praxi.queue.menu;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.match.Match;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.queue.Queue;
import me.funky.praxi.util.CC;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.Constants;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class QueueSelectKitMenu extends Menu {

	private boolean ranked;

	{
		setAutoUpdate(true);
	}

	@Override
	public String getTitle(Player player) {
		return "&8Select a kit (" + (ranked ? "Ranked" : "Unranked") + ")";
	}

	@Override
    public int getSize() {
		return Praxi.get().getMenusConfig().getInteger("QUEUE.SIZE");
    }

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		int i = 10;

		for (int j = 0; j < getSize(); ++j) {
			buttons.put(j, Constants.BLACK_PANE);
		}

		for (Queue queue : Queue.getQueues()) {
			if (queue.isRanked() == ranked) {
				if (ranked && !queue.getKit().getGameRules().isRanked()) continue;
				while (i == 17 || i == 18 || i == 27 || i == 36) {
					i++;
				}
				//buttons.put(i++, new SelectKitButton(queue));
                buttons.put(queue.getKit().getSlot(), new SelectKitButton(queue));
			}
		}
		return buttons;
	}

	@AllArgsConstructor
	private class SelectKitButton extends Button {

		private Queue queue;

		@Override
		public ItemStack getButtonItem(Player player) {
			List<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());
			lore.addAll(queue.getKit().getDescription());
			lore.add("");
			lore.add(" &fIn Fights: &r" + CC.translate("&" + profile.getOptions().theme().getColor().getChar()) + Match.getInFightsCount(queue));
			lore.add(" &fIn Queue: &r" + CC.translate("&" + profile.getOptions().theme().getColor().getChar()) + queue.getPlayers().size());
			lore.add("");
			lore.add(" &aClick here to play.");

			return new ItemBuilder(queue.getKit().getDisplayIcon())
					.name(CC.translate("&" + profile.getOptions().theme().getColor().getChar()) + queue.getKit().getName())
					.lore(lore)
					.clearFlags()
					.build();
		}

		@Override
		public void clicked(Player player, ClickType clickType) {
			Profile profile = Profile.getByUuid(player.getUniqueId());

			if (profile == null) {
				return;
			}

            if(!profile.getFollowing().isEmpty()){
                player.sendMessage(CC.translate("&cYou cannot queue while following someone."));
                return;
            }

			if (player.hasMetadata("frozen")) {
				player.sendMessage(CC.RED + "You cannot queue while frozen.");
				return;
			}

			if (profile.isBusy()) {
				player.sendMessage(CC.RED + "You cannot queue right now.");
				return;
			}

			player.closeInventory();

			queue.addPlayer(player, queue.isRanked() ? profile.getKitData().get(queue.getKit()).getElo() : 0);
            queue.addQueue();
		}

	}
}