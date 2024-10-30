package me.funky.praxi.adapter.spigot;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import me.funky.praxi.Praxi;
import me.funky.praxi.adapter.spigot.impl.Default;
import me.funky.praxi.adapter.spigot.impl.CarbonSpigot;
import me.funky.praxi.adapter.spigot.impl.FoxSpigot;

@UtilityClass
public class SpigotManager {

    @Getter @Setter public static Spigot spigot;
    @Getter @Setter public static String serverSpigot;

    public static void init() {
         switch (Praxi.get().getServer().getName()) {
             case "Carbon": case "CarbonSpigot":
                spigot = new CarbonSpigot();
                setServerSpigot("CarbonSpigot");
                break;
			 case "FoxSpigot":
                spigot = new FoxSpigot();
                setServerSpigot("FoxSpigot");
                break;
            default:
                spigot = new Default();
                setServerSpigot("Default");
                break;
        }
    }

}
