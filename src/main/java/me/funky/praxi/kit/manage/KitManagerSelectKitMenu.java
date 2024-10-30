package me.funky.praxi.kit.manage;

import lombok.AllArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.kit.menu.KitManagementMenu;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.util.Constants;
import me.funky.praxi.util.ItemBuilder;
import me.funky.praxi.util.menu.Button;
import me.funky.praxi.util.menu.Menu;
import me.funky.praxi.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class KitManagerSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&8Manage kits";
    }

    @Override
    public int size(Map<Integer, Button> buttons) {
        return 27;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        for (int k = 0; k < 27; k++) {
            buttons.put(k, Constants.BLACK_PANE);
        }
        final int[] i = {10};
        Kit.getKits().forEach(kit -> {
            if (i[0] == 17 || i[0] == 18) i[0]++;
            buttons.put(i[0]++, new KitDisplayButton(kit));
        });
        return buttons;
    }

    @AllArgsConstructor
    private class KitDisplayButton extends Button {

        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name("&b&l" + kit.getName())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();
            new KitManagerMenu(kit).openMenu(player);
        }
    }
}
