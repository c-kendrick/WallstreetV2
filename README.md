# WallStreetV2

[![Spigot API](https://img.shields.io/badge/Spigot_API-1.20+-orange?style=for-the-badge)]()
[![WorldEdit](https://img.shields.io/badge/WorldEdit_API-Integrated-blue?style=for-the-badge)]()

**WallStreetV2** is a custom Java plugin developed for Spigot/Paper server environments. It introduces a comprehensive corporate system to Minecraft, allowing players to found companies, manage employee hierarchies, claim territory, and operate mathematically weighted, regenerating mines.

This repository serves as a technical showcase of procedural generation, 3D region mathematics, event interception, and role-based access control within a multiplayer economy.

---

## The Concept (What, Why, & How)

**What is it?**
WallStreetV2 is an economy and land-management game mode. It shifts standard survival gameplay into a corporate structure, where players build businesses, manage staff access levels, and control land that holds valuable, auto-regenerating resources.

**Why build it?**
Standard economy plugins often rely on simple balance sheets, while land claiming is usually flat and static. WallStreetV2 was developed to create an environment where the economy is driven by physical land ownership and procedural resource generation, requiring players to actively manage their workforce using whitelist and blacklist permission models.

**How does it work?**
1. **Foundation:** A player registers a new company, instantly becoming the CEO. They can then claim standard 16x16 chunk "real estate" under their corporate banner.
2. **Delegation:** The CEO manages a strict employee hierarchy (Manager, Trusted, Untrusted, Banished), granting or revoking physical interaction permissions on company land.
3. **Resource Operations:** Server administrators define 3D mine regions. Companies can purchase these mines, which automatically regenerate daily with procedurally generated terrain based on exact ore percentages.

---

## Core Architecture & Technical Highlights

### 1. Procedural Generation & WorldEdit Integration
Mine generation hooks directly into the **WorldEdit API** to physically spawn large-scale regions dynamically.
* **Weighted Distribution:** Utilises the `RandomPattern` function to generate terrain based on exact double-value weighted percentage variables. The system natively supports stone, coal, iron, copper, gold, redstone, emerald, lapis, and diamond ores.
* **Automated Batch Processes:** Implements Bukkit server-side schedulers to execute daily tasks, automatically looping through the data structures to regenerate all corporate mines every 24 hours (20L * 60L * 60L * 24L).
* **Test Generation:** Includes a standalone test generation method that calculates and pastes a preset material pattern directly into a player's active WorldEdit selection.

### 2. 3D Region Mathematics & Event Interception
Land boundaries and build permissions rely on real-time mathematical calculations and event listeners.
* **Spatial Tracking:** Intercepts `PlayerMoveEvent` to dynamically track chunk entry and exit. When a player crosses a chunk boundary into new territory, the plugin immediately broadcasts the land ownership changes.
* **Interaction Validation:** Intercepts `PlayerInteractEvent` to calculate if a broken block falls within specific 3D region bounds (`minX`, `minY`, `minZ` to `maxX`, `maxY`, `maxZ`), actively validating if the player has the legal corporate rank to interact with that block.
* **Interactive UI:** Utilises the Adventure API to send interactive, clickable, and hoverable rich-text components upon `PlayerJoinEvent`.

### 3. Role-Based Access Control (RBAC)
Internal company management uses a custom-built corporate ranking system to dictate permission levels, demonstrating a clear understanding of both whitelist and blacklist models.
* **Role Tiers:** Programmed a segmented hierarchy (CEO, Manager, Trusted, Untrusted, Banished) saved natively in the plugin's data structures.
* **Explicit Blacklisting:** Implemented a "Banished" state to actively block specific players from interacting with or entering company properties, overriding standard wilderness permissions.

### 4. Custom YAML Data Persistence
Utilises a highly segmented, bespoke YAML File I/O system tailored to handle complex corporate data safely.
* **Data Segmentation:** Separates data into distinct files (`companies.yml`, `claims.yml`, `mines.yml`, `playerstats.yml`, `playernames.yml`) to organise application state, chunk ownership, and user metadata efficiently.
* **Dynamic Loading:** Uses custom class setups to load, save, and safely hot-reload these specific configuration files to maintain data integrity during server operations.

---

## Dependencies

To compile and run this plugin, the server environment must be running Spigot/Paper and have the following API plugin installed:
* **WorldEdit** (For spatial region selections and procedural `RandomPattern` block population)

---

## Command Router Overview

The plugin routes interactions through centralised `CommandExecutor` classes (`CompanyCommands` and `WSAdminCommands`), validating custom RBAC permissions before executing logic.

### Corporate Foundation & Claims (`/c`)
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/c help` | All | Returns a basic help greeting to the player. |
| `/c new [name]` | All | Registers a new company, generating YAML data and assigning the player as CEO. |
| `/c claim [company]` | Manager/CEO | Calculates current chunk coordinates and appends it to the company's territory map. |
| `/c purchase mine [mine] [company]` | All | Appends a specified mine to a company's asset list within the YAML data. |
| `/c check` | All | Looks up the current chunk's spatial ID to return the current corporate owner. |
| `/c show [company]` | All | Reads and broadcasts the public YAML profile (CEO, Founded Date, Status) of a company. |

### Employee Management (`/c`)
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/c promote [player] [company]` | Manager/CEO | Mutates the YAML hierarchy to grant Manager status to a player. |
| `/c demote [player] [company]` | Manager/CEO | Mutates the YAML hierarchy to revoke Manager status, returning them to Trusted. |
| `/c trust [player] [company]` | Manager/CEO | Grants a player baseline build/break permissions within company claims. |
| `/c untrust [player] [company]` | Manager/CEO | Revokes baseline build permissions, downgrading them to freelance employee status. |
| `/c banish [player] [company]` | Manager/CEO | Explicitly blacklists a player from company premises, blocking all interactions. |
| `/c unbanish [player] [company]` | Manager/CEO | Removes the blacklist for a player, resetting their status to freelance employee. |

### System Administration (`/wsa`)
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/wsa create mine [name]` | Server Operator | Converts a physical WorldEdit selection into spatial coordinates (`min`/`max` vectors), saves it to YAML, and populates default ore percentages. |
| `/wsa spawn mine [name]` | Server Operator | Forces a procedural regeneration of a specific mine using the defined weighted block percentages in `mines.yml`. |
| `/wsa spawntest` | Server Operator | Immediately spawns a test mine (stone, diamond, gold, iron) into the player's current WorldEdit selection. |
| `/wsa reload [file]` | Server Operator | Safely hot-reloads a specific segmented YAML file (`mine`, `claims`, `companies`, `playerstats`, `playernames`) into server memory. ||
