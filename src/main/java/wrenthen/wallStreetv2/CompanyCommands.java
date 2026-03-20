package wrenthen.wallStreetv2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wrenthen.wallStreetv2.WallStreetv2;
import wrenthen.wallStreetv2.files.CompaniesFile;
import wrenthen.wallStreetv2.files.MinesFile;
import wrenthen.wallStreetv2.files.PlayerNamesFile;
import wrenthen.wallStreetv2.files.PlayerStatsFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompanyCommands implements CommandExecutor {
    private final WallStreetv2 plugin;
    public CompanyCommands(WallStreetv2 plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)  sender;
            if (args.length < 1) {
                help(player);
            } else {
                if (args[0].equalsIgnoreCase("help")) {
                    help(player);
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    reload(player);
                }
                if (args[0].equalsIgnoreCase("trust")) {
                    trust(args, player);
                }
                if (args[0].equalsIgnoreCase("untrust")) {
                    untrust(args, player);
                }
                if (args[0].equalsIgnoreCase("promote")) {
                    promote(args, player);
                }
                if (args[0].equalsIgnoreCase("demote")) {
                    demote(args, player);
                }
                if (args[0].equalsIgnoreCase("banish")) {
                    banish(args, player);
                }
                if (args[0].equalsIgnoreCase("unbanish")) {
                    unbanish(args, player);
                }
                if (args[0].equalsIgnoreCase("claim")) {
                    claim(args, player); }
            }
            if (args[0].equalsIgnoreCase("show")) {
                showCompanyDesc(args, player);
            }
            if (args[0].equalsIgnoreCase("new")) {
                newCompany(args, player);
            }
            if (args[0].equalsIgnoreCase("purchase")) {
                purchaseMenu(args, player);
            }
            if (args[0].equalsIgnoreCase("check")) {
                checkChunk(player);
            }
        }
        return true;
    }

    public void help(Player player) {
        player.sendMessage("hi");
    }

    public void purchaseMenu(String[] args, Player player) {
        if (args[1].equalsIgnoreCase("mine")) {
            purchaseMine(args, player);
        }
    }

    public void purchaseMine(String[] args, Player player) {
        ArrayList<String> minesList = new ArrayList<String>();
        String mineName = args[2];
        String companyName = args[3];

        MinesFile.setString(mineName + "." + "Owner:", companyName);

        if (CompaniesFile.get().getList(companyName + "." + "Mines:") != null) {
            minesList = (ArrayList<String>) CompaniesFile.get().getList(companyName + "." + "Mines:");
        }

        minesList.add(mineName);

        CompaniesFile.setList(companyName + "." + "Mines:", minesList);
        MinesFile.save();
        CompaniesFile.save();
    }

    public void checkChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        String chunkID = chunk.getX() + "." + chunk.getZ();

        if (WallStreetv2.isChunkClaimed(chunkID)) {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "This chunk is claimed by " + ChatColor.GOLD + WallStreetv2.whoOwnsChunk(chunkID));
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Chunk not claimed");
        }
    }

    public void claim(String[] args, Player player) {
        if (args.length > 1) {
            String companyName = args[1];
            if (doesCompanyExist(companyName)) {
                // TODO:
                // Check if adjacent to company's claims in any of the four directions.
                // Take money from company
                // Recalculate upkeep

                if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                    Chunk chunk = player.getLocation().getChunk();

                    String chunkID = chunk.getX() + "." + chunk.getZ();

                    if (plugin.isChunkClaimed(chunkID)) {
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "This chunk is claimed by " + ChatColor.GOLD + plugin.whoOwnsChunk(chunkID));
                    } else {
                        plugin.addChunk(chunkID, companyName);
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Successfully claimed for " + ChatColor.GOLD + companyName);
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to claim for " + ChatColor.GOLD + companyName);
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Company misspelt or doesn't exist.");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Provide company name");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "e.g. /c claim (name)");
        }
    }

    public void trust(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        trustSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have been trusted to operate in " + ChatColor.GOLD + companyName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            trustSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c trust (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to trust for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c trust (Playername) (Company name)");
        }
    }

    public void trustSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "Trusted");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have trusted " + targetPlayerName);

        CompaniesFile.setString(companyName + "." + "Trusted:" + "." + targetPlayerName + ":", "true");
        CompaniesFile.save();
    }

    public void untrust(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        untrustSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have been un-trusted and can no longer build/break in " + ChatColor.GOLD + companyName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You can still operate as a freelance employee in " + ChatColor.GOLD + companyName);

                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have un-trusted " + ChatColor.GOLD + targetPlayerName);
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "They can still operate as a freelance employee");
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "To prevent this, you can ban them from company premises with:");
                        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c banish " + ChatColor.GOLD + targetPlayerName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            untrustSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c untrust (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to untrust for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c untrust (Playername) (Company name)");
        }
    }

    public void untrustSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "Untrusted");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have untrusted " + ChatColor.GOLD + targetPlayerName);

        CompaniesFile.setString(companyName + "." + "Trusted:" + "." + targetPlayerName + ":", "false");
        CompaniesFile.save();
    }

    public void promote(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        promoteSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have been promoted to Manager in " + ChatColor.GOLD + companyName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            promoteSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.DARK_AQUA + companyName + ChatColor.GOLD + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c promote (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to promote for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c promote (Playername) (Company name)");
        }
    }

    public void promoteSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "." + "Companies:" + "." + companyName, "Manager");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have promoted " + ChatColor.GOLD + targetPlayerName + ChatColor.DARK_AQUA + " to Manager");

        CompaniesFile.setString(companyName + "." + "Managers:" + "." + targetPlayerName + ":", "true");
        CompaniesFile.save();
    }

    public void demote(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        demoteSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You are no longer a Manager in " + ChatColor.GOLD + companyName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            demoteSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c promote (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to promote for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c demote (Playername) (Company name)");
        }
    }

    public void demoteSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "Trusted");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have demoted " + ChatColor.GOLD + targetPlayerName + ChatColor.GOLD + " from Manager");

        CompaniesFile.setString(companyName + "." + "Managers:" + "." + targetPlayerName + ":", "false");
        CompaniesFile.save();
    }

    public void banish(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        banishSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have been banned from the premises of " + ChatColor.GOLD + companyName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            banishSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c banish (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to banish for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c banish (Playername) (Company name)");
        }
    }

    public void banishSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "Banished");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have banned " + ChatColor.GOLD + targetPlayerName + ChatColor.DARK_AQUA + " from your company's premises");

        CompaniesFile.setString(companyName + "." + "Banned:" + "." + targetPlayerName + ":", "true");
        CompaniesFile.save();
    }

    public void unbanish(String[] args, Player player) {
        if (args.length > 1) {
            String targetPlayerName = args[1];
            String companyName = args[2];

            if (WallStreetv2.isPlayerRank(player, companyName, "CEO") || WallStreetv2.isPlayerRank(player, companyName, "Manager")) {
                if (doesCompanyExist(companyName)) {
                    Player tPlayer = Bukkit.getPlayer(targetPlayerName);
                    String uuidString = null;
                    if (tPlayer != null) {
                        uuidString = tPlayer.getUniqueId().toString();
                        PlayerNamesFile.set(targetPlayerName, uuidString);
                        PlayerNamesFile.save();

                        unBanishSecondFunc(uuidString, companyName, player, targetPlayerName);
                        tPlayer.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have been un-banned from the premises of " + ChatColor.GOLD + companyName);
                    } else {
                        uuidString = PlayerNamesFile.get().getString(targetPlayerName);
                        if (uuidString != null) {
                            unBanishSecondFunc(uuidString, companyName, player, targetPlayerName);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Player not found");
                        }                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Either company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " does not exist,");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "or you have input the wrong command. Correct usage:");
                    player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c unbanish (playername) (company name)");
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Not enough perms to unbanish for " + ChatColor.GOLD + companyName);
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Must be a Manager or CEO");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c unbanish (Playername) (Company name)");
        }
    }

    public void unBanishSecondFunc(String uuidString, String companyName, Player player, String targetPlayerName) {
        PlayerStatsFile.set(uuidString + "." + "Player name:", targetPlayerName);
        PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "Unbanished");
        PlayerStatsFile.save();

        player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "You have" + ChatColor.DARK_RED + " un-banned " + ChatColor.GOLD + targetPlayerName + ChatColor.DARK_AQUA + " from your company's premises");

        CompaniesFile.setString(companyName + "." + "Banned:" + "." + targetPlayerName + ":", "false");
        CompaniesFile.save();
    }

    public void list(Player player) {
        player.sendMessage("");
    }

    public void reload(Player player) {
        CompaniesFile.reload();
    }

    public void newCompany(String[] args, Player player) {
        if (args.length > 1) {
            String companyName = args[1];
            if (!doesCompanyExist(companyName)) {
                List<String> myList = new ArrayList<>();
                List<String> newList = new ArrayList<>();

                //Creating a new company
                UUID playerUUID = player.getUniqueId();
                String uuidString = playerUUID.toString();
                String playerName = player.getDisplayName();

                PlayerNamesFile.set(playerName, uuidString);
                PlayerNamesFile.save();

                PlayerStatsFile.set(uuidString + "." + "Player name:", playerName);
                PlayerStatsFile.set(uuidString + "." + "Companies:" + "." + companyName, "CEO");
                PlayerStatsFile.save();

                CompaniesFile.setString(companyName + "." + "CEO:", playerName);
                CompaniesFile.setString(companyName + "." + "CEO UUID:", uuidString);
                CompaniesFile.setString(companyName + "." + "Cash:", "100");
                CompaniesFile.setString(companyName + "." + "Founded:", "Today!");

//                myList.add("Test1");
//                myList.add("Test2");
//                myList.add("Test3");
//                myList.add("Test4");
//
//                CompaniesFile.setList(companyName + "." + "Mines:", myList);
//                newList = (List<String>) CompaniesFile.get().getList(companyName + "." + "Mines:");
//                for (int i = 0; i < newList.size(); i++) {
//                    Bukkit.broadcastMessage(newList.get(i));
//                }

                CompaniesFile.save();

                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Company founded");
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Company already exists with that name!");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Please provide a name for your company.");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "e.g. /c new (Company name)");
        }
    }

    public boolean doesCompanyExist(String companyName) {
        if (CompaniesFile.get().getString(companyName) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void showCompanyDesc(String[] args, Player player) {
        if (args.length > 1) {
            String companyName = args[1];
            if (doesCompanyExist(companyName)) {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + companyName);
                player.sendMessage(ChatColor.DARK_AQUA + "CEO: " + CompaniesFile.get().getString(companyName + "." + "CEO:"));
                player.sendMessage(ChatColor.DARK_AQUA + "Status: " + CompaniesFile.get().getString(companyName + "." + "Status:"));
                player.sendMessage(ChatColor.DARK_AQUA + "Founded: " + CompaniesFile.get().getString(companyName + "." + "Founded:"));
            } else {
                player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Company " + ChatColor.GOLD + companyName + ChatColor.DARK_AQUA + " misspelt or doesn't exist.");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "Incorrect usage of command. Please try:");
            player.sendMessage(ChatColor.GOLD + "[WALL ST] " + ChatColor.DARK_AQUA + "/c show (Company name)");
        }
    }
}
