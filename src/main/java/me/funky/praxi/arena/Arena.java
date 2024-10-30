package me.funky.praxi.arena;

import lombok.Getter;
import lombok.Setter;
import me.funky.praxi.Praxi;
import me.funky.praxi.arena.impl.SharedArena;
import me.funky.praxi.arena.impl.StandaloneArena;
import me.funky.praxi.arena.cuboid.Cuboid;
import me.funky.praxi.arena.cache.ArenaCache;
import me.funky.praxi.arena.cache.ArenaChunk;
import me.funky.praxi.kit.Kit;
import me.funky.praxi.util.ChunkUtil;
import me.funky.praxi.util.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.Location;
import net.minecraft.server.v1_8_R3.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import net.minecraft.server.v1_8_R3.ChunkSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

@Getter
@Setter
public class Arena extends Cuboid {

    @Getter
    private static final List<Arena> arenas = new ArrayList<>();

    protected String name;
    protected String displayName;
    protected Location location1, location2, spawnA, spawnB;
    protected boolean active, duplicate;
    private ArenaCache cache;
    private List<String> kits = new ArrayList<>();
    private Map<Chunk, ChunkSnapshot> chunkSnapshots = new HashMap<>();

    public Arena(String name, Location location1, Location location2) {
        super(location1, location2);
        this.name = name;
    }

    public Arena(String name, Location location1, Location location2, Location spawnA, Location spawnB) {
        super(location1, location2);
        this.name = name;
        this.spawnA = spawnA;
        this.spawnB = spawnB;
        this.location1 = location1;
        this.location2 = location2;
    }

    public static void init() {
        FileConfiguration configuration = Praxi.get().getArenasConfig().getConfiguration();

        if (configuration.contains("arenas")) {
            for (String arenaName : configuration.getConfigurationSection("arenas").getKeys(false)) {
                String path = "arenas." + arenaName;

                ArenaType arenaType = ArenaType.valueOf(configuration.getString(path + ".type"));
                Location location1 = LocationUtil.deserialize(configuration.getString(path + ".cuboid.location1"));
                Location location2 = LocationUtil.deserialize(configuration.getString(path + ".cuboid.location2"));
                Location spawnA = LocationUtil.deserialize(configuration.getString(path + ".spawnA"));
                Location spawnB = LocationUtil.deserialize(configuration.getString(path + ".spawnB"));
                Arena arena;

                if (arenaType == ArenaType.STANDALONE) {
                    arena = new StandaloneArena(arenaName, location1, location2);
                } else if (arenaType == ArenaType.SHARED) {
                    arena = new SharedArena(arenaName, location1, location2);
                } else {
                    continue;
                }

                if (configuration.contains(path + ".cuboid.location1")) {
                    arena.setLocation1(LocationUtil.deserialize(configuration.getString(path + ".cuboid.location1")));
                }

                if (configuration.contains(path + ".cuboid.location2")) {
                    arena.setLocation2(LocationUtil.deserialize(configuration.getString(path + ".cuboid.location2")));
                }

                if (configuration.contains(path + ".spawnA")) {
                    arena.setSpawnA(LocationUtil.deserialize(configuration.getString(path + ".spawnA")));
                }

                if (configuration.contains(path + ".spawnB")) {
                    arena.setSpawnB(LocationUtil.deserialize(configuration.getString(path + ".spawnB")));
                }


                String displayName = configuration.getString(path + ".displayName");
                arena.setDisplayName(displayName);

                if (configuration.contains(path + ".kits")) {
                    for (String kitName : configuration.getStringList(path + ".kits")) {
                        arena.getKits().add(kitName);
                    }
                }

                if (arena instanceof StandaloneArena && configuration.contains(path + ".duplicates")) {
                    for (String duplicateId : configuration.getConfigurationSection(path + ".duplicates").getKeys(false)) {
                        location1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location1"));
                        location2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location2"));
                        spawnA = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnA"));
                        spawnB = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnB"));

                        Arena duplicate = new Arena(arenaName, location1, location2, spawnA, spawnB);

                        duplicate.setDisplayName(arena.getDisplayName());
                        duplicate.setSpawnA(spawnA);
                        duplicate.setSpawnB(spawnB);
                        duplicate.setLocation1(location1);
                        duplicate.setLocation2(location2);
                        duplicate.setKits(arena.getKits());

                        ((StandaloneArena) arena).getDuplicates().add(duplicate);

                        Arena.getArenas().add(duplicate);
                    }
                }

                Arena.getArenas().add(arena);
            }
        }
    }

    public static Arena getByName(String name) {
        for (Arena arena : arenas) {
            if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null &&
                    arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }

        return null;
    }

    public static Arena getRandomArena(Kit kit) {
        List<Arena> _arenas = new ArrayList<>();

        for (Arena arena : arenas) {
            if (!arena.isSetup()) {
                continue;
            }

            if (!arena.getKits().contains(kit.getName())) {
                continue;
            }

            if (kit.getGameRules().isBuild() && !arena.isActive() && (arena.getType() == ArenaType.STANDALONE ||
                    arena.getType() == ArenaType.DUPLICATE)) {
                _arenas.add(arena);
            } else if (!kit.getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) {
                _arenas.add(arena);
            }
        }

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

    public void takeSnapshot() {
        Cuboid cuboid = new Cuboid(location1, location2);
        ArenaCache chunkCache = new ArenaCache();
        cuboid.getChunks().forEach(chunk -> {
            chunk.load();
            Chunk nmsChunk = ((CraftChunk)chunk).getHandle();
            ChunkSection[] nmsSections = ChunkUtil.copyChunkSections(nmsChunk.getSections());
            chunkCache.chunks.put(new ArenaChunk(chunk.getX(), chunk.getZ()), ChunkUtil.copyChunkSections(nmsSections));
        });

        this.cache = chunkCache;
    }

    public void restoreSnapshot() {
        Cuboid cuboid = new Cuboid(location1, location2);
        cuboid.getChunks().forEach(chunk -> {
            try {
                chunk.load();
                Chunk craftChunk = ((CraftChunk)chunk).getHandle();
                ChunkSection[] sections = ChunkUtil.copyChunkSections(this.cache.getArenaChunkAtLocation(chunk.getX(), chunk.getZ()));
                ChunkUtil.setChunkSections(craftChunk, sections);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ArenaType getType() {
        return ArenaType.DUPLICATE;
    }

    public boolean isSetup() {
        return getLowerCorner() != null && getUpperCorner() != null && spawnA != null && spawnB != null;
    }

    public int getMaxBuildHeight() {
        int highest = (int) (Math.max(spawnA.getY(), spawnB.getY()));
        return highest + 8;
    }

    public int getDeathZone() {
        int lowest = (int) (Math.min(spawnA.getY(), spawnB.getY()));
        return lowest - 8;
    }

    /*public Location getSpawnA() {
        if (spawnA == null) {
            return null;
        }

        return spawnA.clone();
    }

    public Location getSpawnB() {
        if (spawnB == null) {
            return null;
        }

        return spawnB.clone();
    }*/

    public void setActive(boolean active) {
        if (getType() != ArenaType.SHARED) {
            this.active = active;
        }
    }

    public void save() {

    }

    public void delete() {
        arenas.remove(this);
    }

}