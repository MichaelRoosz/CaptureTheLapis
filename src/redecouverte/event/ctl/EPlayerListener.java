package redecouverte.event.ctl;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.player.*;
import redecouverte.event.ctl.GameManager.*;
import org.bukkit.entity.*;
import org.bukkit.Location;

public class EPlayerListener extends PlayerListener {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private final CaptureTheLapis parent;
    private ArrayList<String> allowedCommands;

    public EPlayerListener(CaptureTheLapis parent) {

        this.parent = parent;

        this.allowedCommands = new ArrayList<String>();
        this.allowedCommands.add("msg");
        this.allowedCommands.add("who");
        this.allowedCommands.add("kit");
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {

        try {
            String[] split = event.getMessage().split(" ");
            String command = split[0].substring(1).toLowerCase();

            if (!this.allowedCommands.contains(command)) {
                if (!parent.getGameManager().CanPlayerUseCommands(event.getPlayer())) {
                    event.setMessage("/this-command-does-not-exist");
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You are currently playing Capture the Lapis - command blocked.");
                    return;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        try {
            parent.getGameManager().PlayerJoined(event.getPlayer());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onPlayerQuit(PlayerEvent event) {
        try {
            parent.getGameManager().PlayerDisconnected(event.getPlayer());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        try {
            if (!parent.getGameManager().IsMoveAllowed(event)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        try {
            logger.log(Level.INFO, "DROP_EVENT");
            if (event.isCancelled()) {
                return;
            }

            if (parent.getGameManager().DropIsMagicLapis(event.getPlayer(), event.getItemDrop())) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }
}
