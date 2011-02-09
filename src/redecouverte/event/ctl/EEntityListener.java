package redecouverte.event.ctl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.entity.*;
import org.bukkit.entity.Player;
import org.bukkit.block.*;

public class EEntityListener extends EntityListener {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private final CaptureTheLapis parent;

    public EEntityListener(CaptureTheLapis parent) {
        this.parent = parent;
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {

        try {
            if (event.isCancelled()) {
                return;
            }

            for (Block block : event.blockList()) {
                if (parent.getGameManager().IsMagicLapisBlock(block)) {
                    event.setCancelled(true);
                    break;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {

        try {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            parent.getGameManager().PlayerDied(event);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }
}
