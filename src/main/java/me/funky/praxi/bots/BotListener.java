package me.funky.praxi.bots;

import org.bukkit.util.Vector;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.Potion;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.util.NMS;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Damageable;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.List;
import java.util.Random;
import org.bukkit.scheduler.BukkitRunnable;
import me.funky.praxi.Praxi;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.ItemUtil;

public class BotListener implements Listener {

    private double npcHealth = 20.0;

    @EventHandler
    public void onPlayerDamageNPC(EntityDamageByEntityEvent event) {
        if (event.getEntity().hasMetadata("NPC")) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());

            if (npc.getName().equals("PvPBot") && event.getDamager() instanceof Player) {
                double damage = event.getDamage();
                handleNPCDamage(npc, damage);
            }
        }
    }

    public void handleNPCDamage(NPC npc, double damage) {
        npcHealth -= damage; 
        if (npcHealth <= 0) {
            npc.despawn();
        }
    }

    @EventHandler
    public void onNPCDamage(NPCDamageEvent event) {
        NPC npc = event.getNPC();
        if (npc.getName().equals("PvPBot")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (event.getNPC().getName().equals("PvPBot")) {
            //Bukkit.getServer().broadcastMessage("PvPBot has died.");
        }
    }
}
//}