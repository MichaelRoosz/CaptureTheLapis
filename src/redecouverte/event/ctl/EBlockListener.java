package redecouverte.event.ctl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.block.*;
import org.bukkit.entity.Player;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class EBlockListener extends BlockListener {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private final CaptureTheLapis parent;

    public EBlockListener(CaptureTheLapis parent) {
        this.parent = parent;
    }

    @Override
    public void onBlockInteract(BlockInteractEvent event) {

        try {
            if (event.isCancelled()) {
                return;
            }

            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getEntity();
            Block block = event.getBlock();

            if (parent.getGameManager().BlockBlockInteraction(player, block)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        try {


            if (event.isCancelled()) {
                return;
            }

            if (event.getDamageLevel().getLevel() == BlockDamageLevel.BROKEN.getLevel()) {
                if (parent.getGameManager().CheckBrokenBlockForMagicLapis(event)) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event)  {

        Player p = event.getPlayer();
        Location l = p.getLocation();

        ItemStack is = new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData());
        p.getWorld().dropItem(l, is);

        is = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        p.getWorld().dropItem(l, is);
        
        try {
            if (!event.isCancelled()) {
                parent.getGameManager().CheckBlockPlaceForMagicLapis(event);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
