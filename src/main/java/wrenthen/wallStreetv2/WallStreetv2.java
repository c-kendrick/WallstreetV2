package wrenthen.wallStreetv2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import wrenthen.wallStreetv2.files.*;

import java.util.Objects;

public final class WallStreetv2 extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("c").setExecutor(new CompanyCommands(this));
        getCommand("wsa").setExecutor(new WSAdminCommands(this));
        getServer().getPluginManager().registerEvents(new PreventionListener(this),this);
        getServer().getPluginManager().registerEvents(new ClaimsIdentifierListener(this), this);

        getConfig().options().copyDefaults();

        CompaniesFile.setup();
        CompaniesFile.get().options().copyDefaults(true);
        CompaniesFile.save();

        ClaimsFile.setup();
        ClaimsFile.get().options().copyDefaults(true);
        ClaimsFile.save();

        PlayerStatsFile.setup();
        PlayerStatsFile.get().options().copyDefaults(true);
        PlayerStatsFile.save();

        PlayerNamesFile.setup();
        PlayerNamesFile.get().options().copyDefaults(true);
        PlayerNamesFile.save();

        MinesFile.setup();
        MinesFile.get().options().copyDefaults(true);
        MinesFile.save();


        // Process passive income
        // Calculate company profits, stockprice (or should stockprice be hourly?)
        // Payout dividends?
        minesScheduler();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        CompaniesFile.save();
        ClaimsFile.save();
        PlayerStatsFile.save();
        PlayerNamesFile.save();
        MinesFile.save();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(Component
                .text("Welcome! Click me ;)")
                .color(NamedTextColor.DARK_AQUA)
                .decorate(TextDecoration.ITALIC)
                .clickEvent(ClickEvent.runCommand("/help"))
                .hoverEvent(HoverEvent.showText(Component.text("Rise and shine.\n The stock market's not going\n to the moon by itself!")
                        .color(NamedTextColor.GOLD))));

    }

    public void minesScheduler() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskTimer(this, () -> {
            // Iterate through mines in mines.yml and spawn them all

            for (String key : MinesFile.get().getKeys(false)) {
                WSAdminCommands.spawnMine(key);
            }
        }, 20L * 60L * 60L * 24L /*<-- the initial delay */, 20L * 60L * 60L * 24L /*<-- the interval */);
    }

    public void addChunk(String chunkId, String companyName) {
        ClaimsFile.set(chunkId, companyName);
        ClaimsFile.save();
    }

    public static boolean isChunkClaimed(String chunkId) {
        if (ClaimsFile.get() == null) {
            return true;
        } else {
            if (ClaimsFile.get().getString(chunkId) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static String whoOwnsChunk(String chunkId) {
        if (isChunkClaimed(chunkId)) {
            return ClaimsFile.get().getString(chunkId);
        }
        return "Wilderness";
    }

    public static boolean isPlayerRank(Player player, String companyName, String rank) {
        String uuidString = player.getUniqueId().toString();
        return Objects.equals(PlayerStatsFile.get().getString(uuidString + "." + "Companies:" + "." + companyName), rank);
    }
}
