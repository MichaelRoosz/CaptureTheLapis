package redecouverte.event.ctl;

import java.io.File;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.*;
import org.bukkit.Location;
import com.nijikokun.bukkit.Permissions.Permissions;

public class CaptureTheLapis extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private EPlayerListener mPlayerListener;
    private EBlockListener mBlockListener;
    private EEntityListener mEntityListener;
    private GameManager mGameManager;
    private Permissions mPermissions = null;

    public CaptureTheLapis(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {

        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onEnable() {
        try {
            setupPermissions();

            mGameManager = new GameManager(this);

            PluginManager pm = getServer().getPluginManager();

            mPlayerListener = new EPlayerListener(this);
            pm.registerEvent(Type.PLAYER_COMMAND, mPlayerListener, Priority.Lowest, this);
            pm.registerEvent(Type.PLAYER_MOVE, mPlayerListener, Priority.Highest, this);
            pm.registerEvent(Type.PLAYER_QUIT, mPlayerListener, Priority.Monitor, this);
            pm.registerEvent(Type.PLAYER_JOIN, mPlayerListener, Priority.Monitor, this);
            pm.registerEvent(Type.PLAYER_DROP_ITEM, mPlayerListener, Priority.Highest, this);

            mBlockListener = new EBlockListener(this);
            pm.registerEvent(Type.BLOCK_DAMAGED, mBlockListener, Priority.Highest, this);
            pm.registerEvent(Type.BLOCK_PLACED, mBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.BLOCK_INTERACT, mBlockListener, Priority.Highest, this);

            mEntityListener = new EEntityListener(this);
            pm.registerEvent(Type.ENTITY_EXPLODE, mEntityListener, Priority.Highest, this);
            pm.registerEvent(Type.ENTITY_DEATH, mEntityListener, Priority.Highest, this);

            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    public void onDisable() {
        try {
            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        PluginDescriptionFile pdfFile = this.getDescription();

        if (permissionsPlugin != null) {
            this.logger.info(pdfFile.getName() + ": Using Nijikokun's permissions plugin for permissions");
            this.mPermissions = ((Permissions) permissionsPlugin);
        }
    }

    public Permissions getPermissions() {
        return this.mPermissions;
    }

    public GameManager getGameManager() {
        return this.mGameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        try {
            if (mPermissions == null) {
                sender.sendMessage("Cannot find Permissions plugin.");
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            Player user = (Player) sender;

            if (!command.getName().toLowerCase().equals("ctl")) {
                return false;
            }

            if (args.length < 1) {
                return false;
            }

            String subCommand = args[0].toLowerCase();

            boolean hasPermission = this.mPermissions == null ? false : this.mPermissions.Security.permission(user, "ctl.gamemaster");

            if (!user.isOp() && !hasPermission) {
                sender.sendMessage("You do not have permission to use this command (ctl.gamemaster).");
                return true;
            }


            if (subCommand.equals("create")) {
                if (args.length < 2) {
                    ExplainCreateCommand(user);
                    return true;
                }

                String gameName = args[1].toLowerCase();
                Location spawnLocation = user.getLocation();
                int areaSize = 64;
                int durationHalf1 = 15;
                int durationHalf2 = 15;

                if (args.length > 2) {
                    try {
                        areaSize = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        ExplainCreateCommand(user);
                        return true;
                    }
                }
                if (args.length > 3) {
                    try {
                        durationHalf1 = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        ExplainCreateCommand(user);
                        return true;
                    }
                }

                if (args.length > 4) {
                    try {
                        durationHalf2 = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        ExplainCreateCommand(user);
                        return true;
                    }
                }

                ErrorMessage emsg = new ErrorMessage();
                if (!this.getGameManager().CreateGame(gameName, user.getWorld(), spawnLocation, areaSize, durationHalf1, durationHalf2, emsg)) {
                    user.sendMessage("Error: " + emsg.GetMessage());
                    return true;
                } else {
                    user.sendMessage("Game created.");
                    return true;
                }

            } else if (subCommand.equals("start")) {
                if (args.length < 2) {
                    ExplainStartCommand(user);
                    return true;
                }

                String gameName = args[1].toLowerCase();

                ErrorMessage emsg = new ErrorMessage();
                if (!this.getGameManager().StartGame(gameName, emsg)) {
                    user.sendMessage("Error: " + emsg.GetMessage());
                    return true;
                } else {
                    user.sendMessage("Game started.");
                    return true;
                }

            } else if (subCommand.equals("cancel")) {

                if (args.length < 2) {
                    ExplainCancelCommand(user);
                    return true;
                }
                String gameName = args[1].toLowerCase();

                ErrorMessage emsg = new ErrorMessage();
                if (!this.getGameManager().CancelGame(gameName, emsg)) {
                    user.sendMessage("Error: " + emsg.GetMessage());
                    return true;
                } else {
                    user.sendMessage("Game canceled.");
                    return true;
                }
            } else {
                user.sendMessage("Unknown sub-command.");
                return false;
            }
        } catch (Exception e) {
            sender.sendMessage("An error occured.");
            logger.log(Level.WARNING, "Capture the Lapis: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return true;
        }
    }


    private void ExplainCreateCommand(Player user) {
        user.sendMessage("Usage: /ctl create <game-name> (area size) (duration 1st half) (duration 2nd half)");
    }

    private void ExplainStartCommand(Player user) {
        user.sendMessage("Usage: /ctl start <game-name>");
    }

    private void ExplainCancelCommand(Player user) {
        user.sendMessage("Usage: /ctl cancel <game-name>");
    }
}
