package wrenthen.wallStreetv2;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import wrenthen.wallStreetv2.files.ClaimsFile;
import wrenthen.wallStreetv2.files.CompaniesFile;
import wrenthen.wallStreetv2.files.MinesFile;

import java.util.ArrayList;
import java.util.List;

public class PreventionListener implements Listener {

    private final WallStreetv2 plugin;
    public PreventionListener(WallStreetv2 plugin) {
        this.plugin = plugin;
    }
    List<String> minesList = new ArrayList<>();
    Chunk chunk;
    String chunkID;


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        int minX, minY, minZ, maxX, maxY, maxZ,
                clickedX, clickedY, clickedZ;
        Player player = event.getPlayer();

        if (event.getClickedBlock() != null) {
            chunk = event.getClickedBlock().getChunk();
            chunkID = chunk.getX() + "." + chunk.getZ();

            if (!player.isOp()) {

                if (WallStreetv2.isChunkClaimed(chunkID)) {
                    String companyName = ClaimsFile.get().getString(chunkID);

                    if (WallStreetv2.isPlayerRank(player, companyName, "Banished")) {
                        // Player is banished. Not allowed to build here.
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You are banned from these premises.");

                    } else { // Player not banished
                        if (CompaniesFile.get().getList(companyName + "." + "Mines:") != null) { // Company owns mines
                            minesList = (List<String>) CompaniesFile.get().getList(companyName + "." + "Mines:");

                            for (int i = 0; i < minesList.size(); i++) { // Iterate through company's mines to check for regions
                                if (chunkID.equals(MinesFile.get().getString(minesList.get(i) + "." + "Min_chunk_location:")) || chunkID.equals(MinesFile.get().getString(minesList.get(i) + "." + "Max_chunk_location:"))) {
                                    // One or both of the min maxes are IN this claim

                                    minX = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "minX:");
                                    minY = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "minY:");
                                    minZ = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "minZ:");

                                    maxX = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "maxX:");
                                    maxY = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "maxY:");
                                    maxZ = MinesFile.get().getInt(minesList.get(i) + "." + "Region:" + "." + "maxZ:");

                                    clickedX = event.getClickedBlock().getX();
                                    clickedY = event.getClickedBlock().getY();
                                    clickedZ = event.getClickedBlock().getZ();

                                    if (!isBlockInRegion(clickedX, clickedY, clickedZ, minX, minY, minZ, maxX, maxY, maxZ)) {
                                        // Block is not in mine region
                                        if (!isPlayerRanked(player, companyName)) {
                                            event.setCancelled(true);
                                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You are not allowed to build here.");
                                        }
                                    } else {
                                        // check if mine is operational
                                        if (MinesFile.get().getString(minesList.get(i) + "." + "Owner").equalsIgnoreCase("unowned")) {
                                            // Mine is not operational
                                            event.setCancelled(true);
                                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Mine is not operational. " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " must purchase mine before operating.");
                                        }
                                    }
                                } else {
                                    // Company has mines, but land is not a mine chunk
                                    if (!isPlayerRanked(player, companyName)) {
                                        event.setCancelled(true);
                                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You are not allowed to build here.");
                                    }
                                }
                            }
                        } else {
                            // Company has no mines
                            if (!isPlayerRanked(player, companyName)) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You are not allowed to build here.");
                            }
                        }
                    }
                } else {
                    // Not claimed
                    for (String key : MinesFile.get().getKeys(false)) {
                        minX = MinesFile.get().getInt("key" + "." + "Region:" + "minX:");
                        minY = MinesFile.get().getInt("key" + "." + "Region:" + "minY:");
                        minZ = MinesFile.get().getInt("key" + "." + "Region:" + "minZ:");

                        maxX = MinesFile.get().getInt("key" + "." + "Region:" + "maxX:");
                        maxY = MinesFile.get().getInt("key" + "." + "Region:" + "maxY:");
                        maxZ = MinesFile.get().getInt("key" + "." + "Region:" + "maxZ:");

                        clickedX = event.getClickedBlock().getX();
                        clickedY = event.getClickedBlock().getY();
                        clickedZ = event.getClickedBlock().getZ();

                        if (isBlockInRegion(clickedX, clickedY, clickedZ, minX, minY, minZ, maxX, maxY, maxZ)) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Mine not claimed. Claim and purchase mine to operate.");
                        }
                    }
                }
            }
        }
    }

    public boolean isBlockInRegion(int targetX, int targetY, int targetZ, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (targetX >= minX && targetY >= minY && targetZ >= minZ && targetX <= maxX && targetY <= maxY && targetZ <= maxZ) {
            // The target block is within the region
            return true;
        } else {
            // The target block is not within the region
            return false;
        }
    }

    public boolean isPlayerRanked(Player player, String companyName) {
        return WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager") || WallStreetv2.isPlayerRank(player, companyName, "Trusted");
    }
}
