package redecouverte.event.ctl;

import java.util.ArrayList;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

public class Game {

    public static enum GameStatus {

        CREATED, HALF1, HALF2, FINISHED
    }
    private String name;
    private Location spawnLocation;
    private int areaSize;
    private Integer duration1;
    private Integer duration2;
    private CaptureTheLapis parent;
    private World world;
    private int[] backupObsWallHeight;
    private GameStatus status;
    private ArrayList<String> teamA;
    private ArrayList<String> teamB;
    private ArrayList<BlockBackup> blockBackups;
    private LapisPosition LapisA;
    private LapisPosition LapisB;
    private long startTime, endHalf1Time, endHalf2Time;
    private int minX, maxX, minZ, maxZ;
    private int maxZA, minZB;
    private ArrayList<String> dieTeleList;

    public Game(CaptureTheLapis parent, String name, World world, Location spawnLocation, int areaSize, int duration1, int duration2) {
        this.name = name;
        this.world = world;
        this.spawnLocation = spawnLocation;
        this.areaSize = areaSize;
        this.duration1 = duration1;
        this.duration2 = duration2;
        this.parent = parent;

        this.teamA = new ArrayList<String>();
        this.teamB = new ArrayList<String>();
        this.blockBackups = new ArrayList<BlockBackup>();
        this.dieTeleList = new ArrayList<String>();

        this.backupObsWallHeight = null;

        minX = spawnLocation.getBlockX() - this.areaSize;
        maxX = spawnLocation.getBlockX() + this.areaSize;
        minZ = spawnLocation.getBlockZ();
        maxZ = spawnLocation.getBlockZ() + (4 * this.areaSize) + 2;
        maxZA = minZ + (2 * this.areaSize) + 1;
        minZB = maxZA + 1;

        this.status = GameStatus.CREATED;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getPlayers() {
        ArrayList<String> ret = new ArrayList<String>();

        ret.addAll(this.teamA);
        ret.addAll(this.teamB);

        return ret;
    }

    public void Cancel() {
        this.undoBlockChanges();

        if (this.status == GameStatus.FINISHED) {
            return;
        }

        if (this.status == GameStatus.HALF1) {
            this.removeObsWall();
        }

        if (this.status == GameStatus.HALF1 || this.status == GameStatus.HALF2) {
            this.removeMagicLapis();
        }

        this.status = GameStatus.FINISHED;
    }

    public void removeMagicLapis() {
        if (this.LapisA.isCarriedByPlayer()) {
            ItemStack rStack = new ItemStack(Material.LAPIS_BLOCK);
            rStack.setAmount(1);
            this.LapisA.getPlayer().getInventory().remove(rStack);
        } else {
            this.world.getBlockAt(this.LapisA.getX(), this.LapisA.getY(), this.LapisA.getZ()).setType(Material.AIR);
        }

        if (this.LapisB.isCarriedByPlayer()) {
            ItemStack rStack = new ItemStack(Material.LAPIS_BLOCK);
            rStack.setAmount(1);
            this.LapisB.getPlayer().getInventory().remove(rStack);
        } else {
            this.world.getBlockAt(this.LapisB.getX(), this.LapisB.getY(), this.LapisB.getZ()).setType(Material.AIR);
        }
    }

    public boolean collidesWith(Location cSpawnLocation, int cAreaSize) {

        int cminX = spawnLocation.getBlockX() - this.areaSize;
        int cmaxX = spawnLocation.getBlockX() + this.areaSize;
        int cminZ = spawnLocation.getBlockZ();
        int cmaxZ = spawnLocation.getBlockZ() + (4 * this.areaSize) + 2;

        if (maxX < cminX | maxZ < cminZ) {
            return false;
        }

        if (minX > cmaxX | minZ > cmaxZ) {
            return false;
        }

        return true;
    }

    public void saveChangeBlock(Block b, Material mto, byte dto) {
        BlockBackup nBackup = new BlockBackup(this.world, b.getX(), b.getY(), b.getZ(), b.getType(), b.getData());
        this.blockBackups.add(nBackup);

        b.setType(mto);
        b.setData(dto);
    }

    public void undoBlockChanges() {
        for (BlockBackup back : this.blockBackups) {
            back.Restore();
        }

        this.blockBackups.clear();
    }

    public void drawArena() {
        int x, y, z;

        // draw first line
        for (z = minZ; z <= maxZ; z++) {
            y = this.world.getHighestBlockYAt(minX, z);
            this.saveChangeBlock(this.world.getBlockAt(minX, y, z), Material.WOOL, DyeColor.RED.getData());
        }

        // draw second line
        for (z = minZ; z <= maxZ; z++) {
            y = this.world.getHighestBlockYAt(maxX, z);
            this.saveChangeBlock(this.world.getBlockAt(maxX, y, z), Material.WOOL, DyeColor.RED.getData());
        }

        // draw third line
        for (x = minX + 1; x <= maxX - 1; x++) {
            y = this.world.getHighestBlockYAt(x, minZ);
            this.saveChangeBlock(this.world.getBlockAt(x, y, minZ), Material.WOOL, DyeColor.RED.getData());
        }

        // draw fourth line
        for (x = minX + 1; x <= maxX - 1; x++) {
            y = this.world.getHighestBlockYAt(x, maxZ);
            this.saveChangeBlock(this.world.getBlockAt(x, y, maxZ), Material.WOOL, DyeColor.RED.getData());
        }

        // draw obs wall bottom
        for (x = minX + 1; x <= maxX - 1; x++) {
            y = this.world.getHighestBlockYAt(x, maxZA);
            this.saveChangeBlock(this.world.getBlockAt(x, y, maxZA), Material.WOOL, DyeColor.BLUE.getData());
        }
    }

    public void drawObsWall() {
        int x, y;

        int length = maxX - minX + 1;
        this.backupObsWallHeight = new int[length];

        int i = 0;

        for (x = minX; x <= maxX; x++) {

            y = this.world.getHighestBlockYAt(x, maxZA);
            this.backupObsWallHeight[i] = y;
            i++;

            for (; y < 128.; y++) {
                this.world.getBlockAt(x, y, maxZA).setType(Material.OBSIDIAN);
            }
        }
    }

    public void removeObsWall() {
        if (this.backupObsWallHeight == null) {
            return;
        }

        int x, y;

        int i = 0;
        for (x = minX; x <= maxX; x++) {
            for (y = this.backupObsWallHeight[i]; y < 128; y++) {
                this.world.getBlockAt(x, y, maxZA).setType(Material.AIR);
            }
            i++;
        }
    }

    public boolean start(ErrorMessage ErrorMsg) {

        if (this.status != GameStatus.CREATED) {
            ErrorMsg.SetMessage("Game already started.");
            return false;
        }

        // set teams
        teamA.clear();
        teamB.clear();

        teamA = Toolbox.getPlayersInArea(this.parent.getServer(), this.world, minX, maxX, minZ, maxZA);
        teamB = Toolbox.getPlayersInArea(this.parent.getServer(), this.world, minX, maxX, minZB, maxZ);

        // check teams not empty
        if (teamA.size() < 1 || teamB.size() < 1) {
        ErrorMsg.SetMessage("A team is empty.");
        return false;
        }
         

        this.parent.getServer().broadcastMessage("[Game " + this.name + "] Capture the Lapis has been started!");

        String membersA = "Team A: ";
        for (String s : teamA) {
            membersA = membersA + s + " ";
        }

        String membersB = "Team B: ";
        for (String s : teamB) {
            membersB = membersB + s + " ";
        }

        this.parent.getServer().broadcastMessage(membersA);
        this.parent.getServer().broadcastMessage(membersB);

        // draw obsidian wall
        this.drawObsWall();

        // spawn magic lapis
        int x, y, z;

        // team a lapis
        x = spawnLocation.getBlockX();
        z = minZ + 1;
        y = this.world.getHighestBlockYAt(x, z);
        this.world.getBlockAt(x, y, z).setType(Material.LAPIS_BLOCK);
        this.LapisA = new LapisPosition(x, y, z);


        // team b lapis
        x = spawnLocation.getBlockX();
        z = maxZ - 1;
        y = this.world.getHighestBlockYAt(x, z);
        this.world.getBlockAt(x, y, z).setType(Material.LAPIS_BLOCK);
        this.LapisB = new LapisPosition(x, y, z);

        this.parent.getServer().broadcastMessage("[Game " + this.name + "] The magic lapis lazuli blocks have appeared!");
        this.parent.getServer().broadcastMessage("[Game " + this.name + "] 1st half has begun! Each team now has " + this.duration1.toString() + " minutes to hide their magic lapis lazuli.");

        // start timers
        startTime = System.currentTimeMillis();
        endHalf1Time = startTime + (this.duration1 * 60 * 1000);
        endHalf2Time = endHalf1Time + (this.duration2 * 60 * 1000);

        this.status = GameStatus.HALF1;

        return true;
    }

    public boolean IsMagicLapisBlock(Block block) {

        if (block == null)
            return false;
        
        if (!this.LapisA.isCarriedByPlayer() && this.LapisA.coordsEqual(block.getX(), block.getY(), block.getZ())) {
            return true;
        } else if (!this.LapisB.isCarriedByPlayer() && this.LapisB.coordsEqual(block.getX(), block.getY(), block.getZ())) {
            return true;
        }

        return false;
    }

    public boolean canPlayerUseCommands(String player) {
        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return true;
        }

        return !isPlayerInAnyTeam(player);
    }

    public boolean isPlayerInAnyTeam(String player) {
        if (isPlayerInTeam(this.teamA, player)) {
            return true;
        }
        if (isPlayerInTeam(this.teamB, player)) {
            return true;
        }

        return false;
    }

    public boolean isPlayerInTeam(ArrayList<String> team, String player) {

        for (String s : team) {
            if (s.equals(player)) {
                return true;
            }
        }

        return false;
    }

    public void CheckPlayerJoin(Player player) {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return;
        }

        if (isPlayerInTeam(this.teamA, player.getName())) {
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " has returned and re-joins team A!");
            Toolbox.SaveTeleportPlayerTo(player, world, spawnLocation.getBlockX(), minZ + 1);

        } else if (isPlayerInTeam(this.teamB, player.getName())) {
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " has returned and re-joins team B!");
            Toolbox.SaveTeleportPlayerTo(player, world, spawnLocation.getBlockX(), maxZ -1);
        }
    }

    public static enum CarriesLapis {

        NONE, TEAMA, TEAMB, ALL
    };

    public CarriesLapis IsMagicLapisCarriedBy(String player) {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return CarriesLapis.NONE;
        }

        boolean hasA = false;
        boolean hasB = false;

        if (this.LapisA.isCarriedByPlayer() && this.LapisA.getPlayer().getName().equals(player)) {
            hasA = true;
        } else if (this.LapisB.isCarriedByPlayer() && this.LapisB.getPlayer().getName().equals(player)) {
            hasB = true;
        }

        if (hasA && hasB) {
            return CarriesLapis.ALL;
        } else if (hasA) {
            return CarriesLapis.TEAMA;
        } else if (hasB) {
            return CarriesLapis.TEAMB;
        } else {
            return CarriesLapis.NONE;
        }
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public boolean CheckLapisPlacedInEnemyBase(Player player, boolean lapisOfTeamA, int x, int z) {

        InArena ground;

        if (lapisOfTeamA) {
            ground = InArena.TeamBGround;
        } else {
            ground = InArena.TeamAGround;
        }

        if (this.status == GameStatus.HALF2 && this.isLocationInArena(x, z) == ground) {

            ArrayList<String> teamWinner;
            String teamNameLooser, teamNameWinner;

            if (lapisOfTeamA) {
                teamNameLooser = "A";
                teamNameWinner = "B";
                teamWinner = this.teamB;
            } else {
                teamNameLooser = "B";
                teamNameWinner = "A";
                teamWinner = this.teamA;
            }


            this.removeMagicLapis();

            this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + player.getName() + " dropped the magic lapis lazuli of team " + teamNameLooser + " in the enemy base.");
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Congratulations team " + teamNameWinner + ", you win!");

            if (this.isPlayerInTeam(teamWinner, player.getName())) {
                ItemStack rStack = new ItemStack(Material.LAPIS_BLOCK);
                rStack.setAmount(1);
                world.dropItemNaturally(player.getLocation(), rStack);
                
                this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + player.getName() + " has been awarded with the magic lapis lazuli.");

                for (String p : teamWinner) {
                    try {
                        Player pi = this.parent.getServer().getPlayer(p);
                        if (pi != null) {
                            rStack = new ItemStack(Material.CAKE);
                            rStack.setAmount(1);
                            world.dropItemNaturally(pi.getLocation(), rStack);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.parent.getServer().broadcastMessage("[Game " + this.name + "] team " + teamNameWinner + " has been awarded with cake (yay!).");
            }

            this.undoBlockChanges();
            this.status = GameStatus.FINISHED;
            return true;

        }

        return false;
    }

    public void DropMagicLapis(Player player, CarriesLapis carries, boolean removeFromInventory) {

        if (carries == CarriesLapis.NONE) {
            return;
        }

        // remove magic lapis from inventory and spawn it

        if (carries == CarriesLapis.TEAMA || carries == CarriesLapis.ALL) {

            if (removeFromInventory) {
                ItemStack rStack = new ItemStack(Material.LAPIS_BLOCK);
                rStack.setAmount(1);
                player.getInventory().remove(rStack);
            }

            Location pLoc = player.getLocation();

            int x, y, z;

            x = pLoc.getBlockX();
            y = pLoc.getBlockY();
            z = pLoc.getBlockZ() + 1;
            while (this.LapisB.coordsEqual(x, y, z)) {
                y++;
            }

            this.world.getBlockAt(x, y, z).setType(Material.LAPIS_BLOCK);
            this.LapisA.setCoords(x, y, z);

            if (!this.CheckLapisPlacedInEnemyBase(player, true, x, z)) {
                this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + player.getName() + " dropped the magic lapis lazuli of team A.");
            }

        }
        if (carries == CarriesLapis.TEAMB || carries == CarriesLapis.ALL) {
            if (removeFromInventory) {
                ItemStack rStack = new ItemStack(Material.LAPIS_BLOCK);
                rStack.setAmount(1);
                player.getInventory().remove(rStack);
            }

            Location pLoc = player.getLocation();

            int x, y, z;
            x = pLoc.getBlockX();
            y = pLoc.getBlockY();
            z = pLoc.getBlockZ() + 1;
            while (this.LapisA.coordsEqual(x, y, z)) {
                y++;
            }
            this.world.getBlockAt(x, y, z).setType(Material.LAPIS_BLOCK);
            this.LapisB.setCoords(x, y, z);

            if (!this.CheckLapisPlacedInEnemyBase(player, false, x, z)) {
                this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + player.getName() + " dropped the magic lapis lazuli of team B.");
            }
        }

    }

    public boolean CheckBrokenBlockForObsWall(BlockDamageEvent event) {
        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return false;
        }

        Block b = event.getBlock();

        int z = b.getZ();

        if (z != maxZA) {
            return false;
        }

        int x = b.getX();

        if (x >= minX && x <= maxX) {
            return true;
        }

        return false;
    }

    public boolean CheckBrokenBlockForMagicLapis(BlockDamageEvent event) {
        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return false;
        }

        // check magic lapis
        Block b = event.getBlock();
        Player p = event.getPlayer();

        if (!this.LapisA.isCarriedByPlayer() && this.LapisA.coordsEqual(b.getX(), b.getY(), b.getZ())) {

            if (this.status == GameStatus.HALF2 && this.isPlayerInTeam(teamA, p.getName())) {
                p.sendMessage("You cannot pick up your own magic lapis lazuli in the second half of the game.");
                return true;
            }

            //  set block to air
            b.setType(Material.AIR);

            // put it in player inventory
            ItemStack nStack = new ItemStack(Material.LAPIS_BLOCK);
            nStack.setAmount(1);

            p.getInventory().addItem(nStack);

            // update LapisA
            this.LapisA.setPlayer(p);

            // send broadcast message
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + p.getName() + " picked up the magic lapis lazuli of team A.");

            //cancel the broken block
            return true;
        } else if (!this.LapisB.isCarriedByPlayer() && this.LapisB.coordsEqual(b.getX(), b.getY(), b.getZ())) {

            if (this.status == GameStatus.HALF2 && this.isPlayerInTeam(teamB, p.getName())) {
                p.sendMessage("You cannot pick up your own magic lapis lazuli in the second half of the game.");
                return true;
            }

            //  set block to air
            b.setType(Material.AIR);

            // put it in player inventory
            ItemStack nStack = new ItemStack(Material.LAPIS_BLOCK);
            nStack.setAmount(1);

            p.getInventory().addItem(nStack);

            // update LapisA
            this.LapisB.setPlayer(p);

            // send broadcast message
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + p.getName() + " picked up the magic lapis lazuli of team B.");

            //cancel the broken block
            return true;
        }
        return false;
    }

    public void CheckBlockPlaceForMagicLapis(BlockPlaceEvent event) {
        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return;
        }

        // check if it is
        CarriesLapis c = IsMagicLapisCarriedBy(event.getPlayer().getName());

        if (c == CarriesLapis.NONE) {
            return;
        }

        if (event.getBlock().getType() == Material.LAPIS_BLOCK) {
            if (c == CarriesLapis.ALL) {
                if (this.isPlayerInTeam(this.teamA, event.getPlayer().getName())) {
                    c = CarriesLapis.TEAMA;
                } else {
                    c = CarriesLapis.TEAMB;
                }
            }

            Block b = event.getBlockPlaced();
            boolean isTeamALapis = c == CarriesLapis.TEAMA ? true : false;

            if (isTeamALapis) {
                this.LapisA.setCoords(b.getX(), b.getY(), b.getZ());
                if (!this.CheckLapisPlacedInEnemyBase(event.getPlayer(), true, b.getX(), b.getZ())) {
                    this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + event.getPlayer().getName() + " placed the magic lapis lazuli of team A.");
                }
            } else {
                this.LapisB.setCoords(b.getX(), b.getY(), b.getZ());
                if (!this.CheckLapisPlacedInEnemyBase(event.getPlayer(), false, b.getX(), b.getZ())) {
                    this.parent.getServer().broadcastMessage("[Game " + this.name + "] " + event.getPlayer().getName() + " placed the magic lapis lazuli of team A.");
                }
            }

        }

    }

    public void DropMagicLapis(EntityDeathEvent event, CarriesLapis carries) {

        if (carries == CarriesLapis.NONE) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (carries == CarriesLapis.TEAMA || carries == CarriesLapis.ALL) {
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.LAPIS_BLOCK) {
                    int newAmount = stack.getAmount() - 1;

                    if (newAmount < 1) {
                        event.getDrops().remove(stack);
                    } else {
                        stack.setAmount(newAmount);
                    }

                    break;
                }
            }
        }

        if (carries == CarriesLapis.TEAMB || carries == CarriesLapis.ALL) {
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.LAPIS_BLOCK) {
                    int newAmount = stack.getAmount() - 1;

                    if (newAmount < 1) {
                        event.getDrops().remove(stack);
                    } else {
                        stack.setAmount(newAmount);
                    }

                    break;
                }
            }
        }

        DropMagicLapis(player, carries, false);

    }

    public boolean CheckDropIsMagicLapis(Player player, ItemDrop drop) {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return false;
        }

        // check if it is
        CarriesLapis c = IsMagicLapisCarriedBy(player.getName());

        if (c == CarriesLapis.NONE) {
            return false;
        }

        // if yes, return true, and remove magc lapis from inventory, and spawn it
        DropMagicLapis(player, c, true);

        return true;
    }

    public void CheckPlayerDisconnect(Player player) {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return;
        }

        if (isPlayerInTeam(this.teamA, player.getName())) {

            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " of team A disconnected.");
            DropMagicLapis(player, IsMagicLapisCarriedBy(player.getName()), true);

        } else if (isPlayerInTeam(this.teamB, player.getName())) {

            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " of team B disconnected.");
            DropMagicLapis(player, IsMagicLapisCarriedBy(player.getName()), true);
        }
    }

    public void CheckPlayerDied(EntityDeathEvent event) {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (isPlayerInTeam(this.teamA, player.getName())) {

            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " of team A died.");
            DropMagicLapis(event, IsMagicLapisCarriedBy(player.getName()));
            this.dieTeleList.add(player.getName());

        } else if (isPlayerInTeam(this.teamB, player.getName())) {

            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Player " + player.getName() + " of team B died.");
            DropMagicLapis(event, IsMagicLapisCarriedBy(player.getName()));
            this.dieTeleList.add(player.getName());
        }
    }

    public static enum InArena {

        NotInArena, TeamAGround, TeamBGround
    };

    public InArena isLocationInArena(Location l) {

        return this.isLocationInArena(l.getBlockX(), l.getBlockZ());
    }

    public InArena isLocationInArena(int locX, int locZ) {

        if (locX >= minX && locX <= maxX && locZ >= minZ && locZ <= maxZA) {
            return InArena.TeamAGround;

        } else if (locX >= minX && locX <= maxX && locZ >= minZB && locZ <= maxZ) {
            return InArena.TeamBGround;
        }

        return InArena.NotInArena;
    }

    public boolean canPlayerMove(PlayerMoveEvent event) {
        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return true;
        }


        if (this.dieTeleList.size() > 0) {
            for (String p : this.dieTeleList) {
                if (p.equals(event.getPlayer().getName())) {
                    if (isPlayerInTeam(this.teamA, p)) {

                        int x = spawnLocation.getBlockX();
                        int z = minZ + 1;
                        int y = Toolbox.SaveTeleportPlayerTo(event.getPlayer(), world, x, z);
                        Location loc = new Location(this.world, x, y, z);
                        event.setFrom(loc);

                    } else if (isPlayerInTeam(this.teamB, p)) {

                        int x = spawnLocation.getBlockX();
                        int z = maxZ - 1;
                        int y = Toolbox.SaveTeleportPlayerTo(event.getPlayer(), world, x, z);
                        Location loc = new Location(this.world, x, y, z);
                        event.setFrom(loc);
                    }
                    this.dieTeleList.remove(p);
                    break;
                }
            }
        }

        
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!to.getWorld().equals(this.world)) {
            return true;
        }


        InArena fromInArena = this.isLocationInArena(from);
        InArena toInArena = this.isLocationInArena(to);

        // if not in the game, but tries to enter arena, block it
        if (fromInArena != InArena.NotInArena || toInArena != InArena.NotInArena) {

            Player player = event.getPlayer();

            boolean playerIsInAnyTeam = this.isPlayerInAnyTeam(player.getName());

            if (fromInArena != InArena.NotInArena && !playerIsInAnyTeam) {
                // a player not in any team is in the arena, tp him out
                event.setFrom(this.world.getSpawnLocation());
                player.teleportTo(this.world.getSpawnLocation());
                return false;

            } else if (toInArena != InArena.NotInArena && !playerIsInAnyTeam) {
                // a player not in any team tries to enter the arena, stop him
                player.teleportTo(from);
                return false;

            } else if (this.status == GameStatus.HALF1 && playerIsInAnyTeam && toInArena == InArena.NotInArena ) {
                // if in a game that is 1st half and tries to leave arena, block it
                if (isPlayerInTeam(this.teamA, player.getName())) {

                    int x = spawnLocation.getBlockX();
                    int z = minZ + 1;
                    int y = Toolbox.SaveTeleportPlayerTo(player, world, x, z);
                    Location loc = new Location(this.world, x, y, z);
                    event.setFrom(loc);

                } else if (isPlayerInTeam(this.teamB, player.getName())) {

                    int x = spawnLocation.getBlockX();
                    int z = maxZ - 1;
                    int y = Toolbox.SaveTeleportPlayerTo(player, world, x, z);
                    Location loc = new Location(this.world, x, y, z);
                    event.setFrom(loc);
                }
                return false;
            }
        }

        return true;
    }

    public void TimerTick() {

        if (this.status != GameStatus.HALF1 && this.status != GameStatus.HALF2) {
            return;
        }

        long now = System.currentTimeMillis();

        // check for 2nd half begin
        if (this.status == GameStatus.HALF1 && now >= this.endHalf1Time) {
            this.StartHalf2();
        } // check for 2nd half end
        else if (this.status == GameStatus.HALF2 && now >= this.endHalf2Time) {
            this.parent.getServer().broadcastMessage("[Game " + this.name + "] Time is up! The game ends with no winner.");
            this.Cancel();
        }

    }

    public void StartHalf2() {

        // remove obs wall for 2nd half
        this.removeObsWall();

        // drop own lapis carried for 2nd half
        if (this.LapisA.isCarriedByPlayer() && this.isPlayerInTeam(teamA, this.LapisA.getPlayer().getName())) {
            this.DropMagicLapis(this.LapisA.getPlayer(), CarriesLapis.TEAMA, true);
        }
        if (this.LapisB.isCarriedByPlayer() && this.isPlayerInTeam(teamB, this.LapisB.getPlayer().getName())) {
            this.DropMagicLapis(this.LapisB.getPlayer(), CarriesLapis.TEAMB, true);
        }

        this.status = GameStatus.HALF2;

        this.parent.getServer().broadcastMessage("[Game " + this.name + "] 2nd half has begun! You now have " + this.duration1.toString() + " minutes to steal the enemy's lapis lazuli.");
    }
}
