package me.funky.praxi.divisions.menu.buttons;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.funky.praxi.Praxi;
import me.funky.praxi.profile.Profile;
import me.funky.praxi.divisions.ProfileDivision;
import me.funky.praxi.util.ProgressBar;
import me.funky.praxi.util.ItemBuilderDev;
import me.funky.praxi.util.menu.Button;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProfileXPDivisionsButton extends Button {

    private static final String KEY = "PROFILE_DIVISIONS.";

    private final Profile profile;
    private final ProfileDivision division;

    //@Override
    public ItemStack getButtonItem(Player player) {
        ProfileDivision profileDivision = profile.getDivision();

        boolean equipped = division.equals(profileDivision);
        boolean unlocked = profileDivision.getExperience() >= division.getExperience();

        //ItemBuilderDev itemBuilder = new ItemBuilderDev(Material.PAPER);
        ItemBuilderDev itemBuilder = new ItemBuilderDev(division.getIcon());
        itemBuilder.durability(division.getDurability());

        if (unlocked && profile.getDivision() != division) {
            itemBuilder.name(Praxi.get().getMenusConfig().getString(KEY + "BUTTONS.XP.UNLOCKED.NAME").replace("{division_display_name}", division.getDisplayName()));
            itemBuilder.lore(Praxi.get().getMenusConfig().getStringList(KEY + "BUTTONS.XP.UNLOCKED.LORE")
                    .stream()
                    .map(s -> {
                        s = s.replace("{division_bar}", ProgressBar.getBarMenu(5,5))
                             .replace("{division_experience}", String.valueOf(division.getExperience()))
                             .replace("{player_experience}", String.valueOf(profile.getExperience()));;
                        return s;
            }).collect(Collectors.toList()));
        } else if (equipped) {
            itemBuilder.name(Praxi.get().getMenusConfig().getString(KEY + "BUTTONS.XP.EQUIPPED.NAME").replace("{division_display_name}", division.getDisplayName()));
            itemBuilder.lore(Praxi.get().getMenusConfig().getStringList(KEY + "BUTTONS.XP.EQUIPPED.LORE")
                    .stream()
                    .map(s -> {
                        s = s.replace("{division_bar}", ProgressBar.getBarMenu(5,5))
                             .replace("{division_experience}", String.valueOf(division.getExperience()))
                             .replace("{player_experience}", String.valueOf(profile.getExperience()));;
                        return s;
            }).collect(Collectors.toList()));
            itemBuilder.enchantment(Enchantment.DURABILITY, 10);
	    } else {
            itemBuilder.name(Praxi.get().getMenusConfig().getString(KEY + "BUTTONS.XP.LOCKED.NAME").replace("{division_display_name}", division.getDisplayName()));
            itemBuilder.lore(Praxi.get().getMenusConfig().getStringList(KEY + "BUTTONS.XP.LOCKED.LORE")
                .stream()
                .map(s -> {
				    s = s.replace("{division_bar}", ProgressBar.getBarMenu(profile.getExperience(), division.getExperience()))
                         .replace("{division_experience}", String.valueOf(division.getExperience()))
                         .replace("{player_experience}", String.valueOf(profile.getExperience()));
                    return s;
        }).collect(Collectors.toList()));
        }
        itemBuilder.clearFlags();
        return itemBuilder.build();
    }
}
