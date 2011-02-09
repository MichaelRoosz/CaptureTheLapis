package redecouverte.event.ctl;

import java.util.ArrayList;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.*;

public class Toolbox {

    public static int SaveTeleportPlayerTo(Player player, World world, int x, int z) {

        int y = world.getHighestBlockYAt(x, z) + 1;

        if (y > 120) {
            y = 120;
            for (int i = 0; i < 5; i++) {
                world.getBlockAt(x, y, z + i).setType(Material.AIR);
            }
        }

        Location teleLoc = new Location(world, x, y, z);
        player.teleportTo(teleLoc);

        return y;
    }

    public static ArrayList<String> getPlayersInArea(Server server, World world, int minX, int maxX, int minZ, int maxZ) {
        ArrayList<String> ret = new ArrayList<String>();

        for (Player p : server.getOnlinePlayers()) {
            if (!p.getWorld().equals(world)) {
                continue;
            }

            int px = p.getLocation().getBlockX();
            int pz = p.getLocation().getBlockZ();


            if ((maxX < px | maxZ < pz) || (minX > px | minZ > pz)) {
                continue;
            }

            ret.add(p.getName());

        }

        return ret;
    }

}
