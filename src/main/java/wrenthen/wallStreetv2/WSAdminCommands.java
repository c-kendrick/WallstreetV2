package wrenthen.wallStreetv2;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wrenthen.wallStreetv2.files.*;

import static org.bukkit.Bukkit.getWorld;

public class WSAdminCommands implements CommandExecutor {
    private final WallStreetv2 plugin;
    public WSAdminCommands(WallStreetv2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)  sender;
            if (player.isOp()) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("create")) {
                        createMenu(player, args);
                    }
                    if (args[0].equalsIgnoreCase("spawn")) {
                        spawnMenu(player, args);
                    }
                    if (args[0].equalsIgnoreCase("spawntest")) {
                        spawnTestMine(player);
                    }
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadMenu(args);
                    }
                } else {
                    return true;
                }
            }
            // TODO: Purchasable mines, freelance-employment system, and passive income from owned mines
            // For purcashing mines, check min and max points to see if it's in their claimed land?
        }
        return true;
    }

    public void reloadMenu(String[] args) {
        if (args[1].equalsIgnoreCase("mine")) {
            MinesFile.reload();
        }
        if (args[1].equalsIgnoreCase("claims")) {
            ClaimsFile.reload();
        }
        if (args[1].equalsIgnoreCase("companies")) {
            CompaniesFile.reload();
        }
        if (args[1].equalsIgnoreCase("playerstats")) {
            PlayerStatsFile.reload();
        }
        if (args[1].equalsIgnoreCase("playernames")) {
            PlayerNamesFile.reload();
        }
    }

    public void purchaseMine(Player player, String[] args) {
        if (WallStreetv2.isPlayerRank(player, args[2], "CEO") || WallStreetv2.isPlayerRank(player, args[2], "Manager")) {
            // Check if company has money
            // Check min and max coordinates are claimed through isChunkClaimed - if not, proceed with payment
        }
    }

    public void spawnMenu(Player player, String[] args) {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("mine")) {
                if (!args[2].isEmpty()) {
                    spawnMine(args[2]);
                } else {
                    player.sendMessage("Please specify mine name");
                }
            }
        }
    }

    public void createMenu(Player player, String[] args) {
        if (args[1].equalsIgnoreCase("mine")) {
            createMine(player, args);
        }
    }

    public static void createMine(Player player, String[] args) {
        // Creates mine file but doesn't spawn the mine in world
        int minX, minY, minZ, maxX, maxY, maxZ;


        if (args.length > 1) {
            String mineName = args[2];
            MinesFile.setString(mineName + "." + "Name:", mineName);

            String worldName = player.getWorld().getName().toString();
            MinesFile.setString(mineName + "." + "World:", worldName);

            Region region = getRegion(player);
            BlockVector3 minVec = region.getMinimumPoint();
            BlockVector3 maxVec = region.getMaximumPoint();

            minX = minVec.getX();
            minY = minVec.getY();
            minZ = minVec.getZ();

            maxX = maxVec.getX();
            maxY = maxVec.getY();
            maxZ = maxVec.getZ();

            MinesFile.setInt(mineName + "." + "Region:" + "." + "minX:", minX);
            MinesFile.setInt(mineName + "." + "Region:" + "." + "minY:", minY);
            MinesFile.setInt(mineName + "." + "Region:" + "." + "minZ:", minZ);

            MinesFile.setInt(mineName + "." + "Region:" + "." + "maxX:", maxX);
            MinesFile.setInt(mineName + "." + "Region:" + "." + "maxY:", maxY);
            MinesFile.setInt(mineName + "." + "Region:" + "." + "maxZ:", maxZ);

            Location minLoc = new Location(player.getWorld(), minX, minY, minZ);
            Location maxLoc = new Location(player.getWorld(), maxX, maxY, maxZ);

            Chunk chunkMin = minLoc.getChunk();
            Chunk chunkMax = maxLoc.getChunk();

            String chunkIdMin = chunkMin.getX() + "." + chunkMin.getZ();
            String chunkIdMax = chunkMax.getX() + "." + chunkMax.getZ();

            MinesFile.setString(mineName + "." + "Min_chunk_location:", chunkIdMin);
            MinesFile.setString(mineName + "." + "Max_chunk_location:", chunkIdMax);

            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "stone:", 0.5);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "coal_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "iron_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "copper_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "gold_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "redstone_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "emerald_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "lapis_ore:", 0.0);
            MinesFile.setDouble(mineName + "." + "Blocks:" + "." + "diamond_ore:", 0.0);

            MinesFile.setString(mineName + "." + "Owner:","unowned");

            MinesFile.save();
        } else {
            player.sendMessage("Please provide mine name");
        }
    }

    public static void spawnMine(String mineName) {
        double stoneCh, diamondOreCh, goldOreCh, ironOreCh, copperOreCh,
                redstoneOreCh, emeraldOreCh, coalOreCh, lapisOreCh;

        int minX, minY, minZ, maxX, maxY, maxZ;

        minX = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "minX:");
        minY = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "minY:");
        minZ = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "minZ:");

        maxX = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "maxX:");
        maxY = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "maxY:");
        maxZ = MinesFile.get().getInt(mineName + "." + "Region:" + "." + "maxZ:");

        BlockVector3 minVec = BlockVector3.at(minX, minY, minZ);
        BlockVector3 maxVec = BlockVector3.at(maxX, maxY, maxZ);
        CuboidRegion region = new CuboidRegion(minVec, maxVec);

        BlockState stone = BukkitAdapter.adapt(Material.STONE.createBlockData());
        BlockState diamond_ore = BukkitAdapter.adapt(Material.DIAMOND_ORE.createBlockData());
        BlockState gold_ore = BukkitAdapter.adapt(Material.GOLD_ORE.createBlockData());
        BlockState iron_ore = BukkitAdapter.adapt(Material.IRON_ORE.createBlockData());
        BlockState copper_ore = BukkitAdapter.adapt(Material.COPPER_ORE.createBlockData());
        BlockState redstone_ore = BukkitAdapter.adapt(Material.REDSTONE_ORE.createBlockData());
        BlockState emerald_ore = BukkitAdapter.adapt(Material.EMERALD_ORE.createBlockData());
        BlockState coal_ore = BukkitAdapter.adapt(Material.COAL_ORE.createBlockData());
        BlockState lapis_ore = BukkitAdapter.adapt(Material.LAPIS_ORE.createBlockData());

        stoneCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "stone:");
        diamondOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "diamond_ore:");
        goldOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "gold_ore:");
        ironOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "iron_ore:");
        copperOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "copper_ore:");
        redstoneOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "redstone_ore:");
        emeraldOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "emerald_ore:");
        coalOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "coal_ore:");
        lapisOreCh = MinesFile.get().getDouble(mineName + "." + "Blocks:" + "." + "lapis_ore:");

        RandomPattern pat = new RandomPattern();

        pat.add(stone, stoneCh);
        pat.add(diamond_ore, diamondOreCh);
        pat.add(gold_ore, goldOreCh);
        pat.add(iron_ore, ironOreCh);
        pat.add(copper_ore, copperOreCh);
        pat.add(redstone_ore, redstoneOreCh);
        pat.add(emerald_ore, emeraldOreCh);
        pat.add(coal_ore, coalOreCh);
        pat.add(lapis_ore, lapisOreCh);

        String worldName = "world";
        worldName = MinesFile.get().getString(mineName + "." + "World:");

        org.bukkit.World worldBukkit = getWorld(worldName);

        com.sk89q.worldedit.world.World worldWE = BukkitAdapter.adapt(worldBukkit);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldWE)) {
            editSession.setBlocks(region, pat);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    public static void spawnTestMine(Player player) {
        World world = BukkitAdapter.adapt(player.getWorld());
        WorldEdit.getInstance().newEditSession(world);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Region region = getRegion(player);

            RandomPattern pat = new RandomPattern();

            BlockState stone = BukkitAdapter.adapt(Material.STONE.createBlockData());
            BlockState diamond_ore = BukkitAdapter.adapt(Material.DIAMOND_ORE.createBlockData());
            BlockState gold_ore = BukkitAdapter.adapt(Material.GOLD_ORE.createBlockData());
            BlockState iron_ore = BukkitAdapter.adapt(Material.IRON_ORE.createBlockData());

            pat.add(stone, 0.5);
            pat.add(diamond_ore, 0.1);
            pat.add(gold_ore, 0.2);
            pat.add(iron_ore, 0.2);

            editSession.setBlocks(region, pat);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    public static Region getRegion(Player player) {
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = manager.get((SessionOwner) actor);

        Region region = null;
        World selectionWorld = localSession.getSelectionWorld();
        try {
            if (selectionWorld == null) throw new IncompleteRegionException();
            region = localSession.getSelection(selectionWorld);
        } catch (IncompleteRegionException ex) {
            ((BukkitPlayer) actor).printError(TextComponent.of("Please make a region selection first."));
        }
        return region;
    }
}
