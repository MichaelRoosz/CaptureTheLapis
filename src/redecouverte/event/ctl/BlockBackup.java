package redecouverte.event.ctl;

import java.util.ArrayList;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

public class BlockBackup {

    private World world;
    private int x, y, z;
    private Material material;
    private byte data;

    public BlockBackup(World world, int x, int y, int z, Material material, byte data) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.data = data;
    }

    public void Restore() {
        Block b = this.world.getBlockAt(this.x, this.y, this.z);
        b.setType(this.material);
        b.setData(this.data);
    }
}
