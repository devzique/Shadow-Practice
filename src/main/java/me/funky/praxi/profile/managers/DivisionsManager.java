package me.funky.praxi.profile.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.funky.praxi.Praxi;
import me.funky.praxi.divisions.ProfileDivision;
import me.funky.praxi.util.CC;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DivisionsManager {

    private final Praxi plugin;

    private static final List<ProfileDivision> divisions = new ArrayList<>();
    private boolean XPBased;

    public void init() {
        if (!divisions.isEmpty()) divisions.clear();

        ConfigurationSection divisionSection = Praxi.get().getDivisionsConfig().getConfigurationSection("DIVISIONS");
        if (divisionSection == null || divisionSection.getKeys(false).isEmpty()) return;
        this.XPBased = divisionSection.getBoolean("XP-BASED");

       ConfigurationSection section = divisionSection.getConfigurationSection("RANKS");
       if (section == null || section.getKeys(false).isEmpty()) return;

       for ( String key : section.getKeys(false) ) {
           String path = key + ".";

           ProfileDivision division = new ProfileDivision(key);
           division.setDisplayName(CC.translate(section.getString(path + "DISPLAY_NAME")));
           division.setIcon(Material.valueOf(CC.translate(section.getString(path + "ICON")).toUpperCase()));
           division.setDurability(section.getInt(path + "DURABILITY"));
           division.setMiniLogo(CC.translate(section.getString(path + "MINI-LOGO")));
           division.setMinElo(section.getInt(path + "ELO-MIN"));
           division.setMaxElo(section.getInt(path + "ELO-MAX"));
           division.setExperience(section.getInt(path + "XP-AMOUNT"));
           division.setXpLevel(section.getInt(path + "XP-LEVEL"));

           //In order to prevent more than one default division
           if (!this.isDefaultPresent()) division.setDefaultDivision(section.getBoolean(path + "DEFAULT"));

           divisions.add(division);
       }
    }

    public ProfileDivision getDivisionByXP(int xp) {
        if (this.getHighest() != null && xp > this.getHighest().getExperience()) return this.getHighest();

        List<ProfileDivision> xpDivisions = new ArrayList<>(divisions);
        xpDivisions.sort(Comparator.comparing(ProfileDivision::getExperience).reversed());

        return xpDivisions.stream().filter(level -> xp >= level.getExperience()).findFirst().orElse(getDefault());
    }

    public ProfileDivision getDivisionByELO(int elo) {
        if (this.getHighest() != null && elo > this.getHighest().getMaxElo()) return this.getHighest();

        for ( ProfileDivision eloRank : divisions) {
            if (elo >= eloRank.getMinElo() && elo <= eloRank.getMaxElo()) {
                return eloRank;
            }
        }
        return getDefault();
    }

    public ProfileDivision getNextDivisionByXP(int xp) {
        List<ProfileDivision> xpDivisions = new ArrayList<>(divisions);
        xpDivisions.sort(Comparator.comparingInt(ProfileDivision::getExperience));

        for (ProfileDivision division : xpDivisions) {
            if (xp < division.getExperience()) {
                return division;
            }
        }

        return null;
    }

    public ProfileDivision getNextDivisionByELO(int elo) {
        List<ProfileDivision> eloDivisions = new ArrayList<>(divisions);
        eloDivisions.sort(Comparator.comparingInt(ProfileDivision::getMinElo));

        for (ProfileDivision division : eloDivisions) {
            if (elo < division.getMinElo()) {
                return division;
            }
        }

        return null;
    }

    public ProfileDivision getDefault() {
        return divisions.stream().filter(ProfileDivision::isDefaultDivision).findAny().orElse(new ProfileDivision("Default"));
    }

    public ProfileDivision getHighest() {
        LinkedList<ProfileDivision> newDivisions = new LinkedList<>(divisions);

        if (this.XPBased) {
            newDivisions.sort(Comparator.comparingInt(ProfileDivision::getExperience).reversed());
        } else {
            newDivisions.sort(Comparator.comparingInt(ProfileDivision::getMaxElo).reversed());
        }

        return newDivisions.getFirst();
    }

    public boolean isDefaultPresent() {
        if (divisions.isEmpty()) return false;
        return divisions.stream().anyMatch(ProfileDivision::isDefaultDivision);
    }

    public static List<ProfileDivision> getDivisions() {
        return divisions;
    }
}
