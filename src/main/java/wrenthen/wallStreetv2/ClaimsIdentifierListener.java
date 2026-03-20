package wrenthen.wallStreetv2;


import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import wrenthen.wallStreetv2.WallStreetv2;

import java.util.HashMap;
import java.util.Objects;

public class ClaimsIdentifierListener implements Listener {
    private final WallStreetv2 plugin;
    public ClaimsIdentifierListener(WallStreetv2 plugin) {this.plugin = plugin;}
    Player player;
    String chunkFromID, chunkToID;
    Chunk chunkFrom, chunkTo;


    @EventHandler
    public void onInteract(PlayerMoveEvent event) {
        player = event.getPlayer();

        chunkFrom = event.getFrom().getChunk();
        chunkTo = event.getTo().getChunk();

        if (chunkFrom != chunkTo) {
            chunkFromID = chunkFrom.getX() + "." + chunkFrom.getZ();
            chunkToID = chunkTo.getX() + "." + chunkTo.getZ();

            if (!Objects.equals(WallStreetv2.whoOwnsChunk(chunkFromID), WallStreetv2.whoOwnsChunk(chunkToID))) {
                player.sendMessage(WallStreetv2.whoOwnsChunk(chunkToID));
            }
        }
    }
}
