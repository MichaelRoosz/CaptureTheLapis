package redecouverte.event.ctl;

import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import redecouverte.event.ctl.Game.CarriesLapis;
import redecouverte.event.ctl.Game.GameStatus;

public class GameManager
{
    private ArrayList<Game> activeGames;
    private CaptureTheLapis parent;

    public GameManager(CaptureTheLapis parent)
    {
        this.activeGames = new ArrayList<Game>();
        this.parent = parent;
    }



    public String GetGameList() {
        String ret = "";

        for (Game g : this.activeGames) {
            ret += g.getName() + " ";
        }

        return ret;
    }

    public boolean CreateGame(String gameName, World world, Location spawnLocation, int areaSize, int durationHalf1, int durationHalf2, ErrorMessage ErrorMsg)
    {
        // check if name already exists
        gameName = gameName.toLowerCase();
        
        for(Game g : this.activeGames)
        {
           if(g.getName().equals(gameName))
           {
             ErrorMsg.SetMessage("A game with this name already exists.");
             return false;
           }
        }

        // check if game overlaps with another game
        for(Game g : this.activeGames)
        {
           if(g.collidesWith(spawnLocation, areaSize))
           {
             ErrorMsg.SetMessage("Overlaps with another game.");
             return false;
           }
        }
        
        // create game class, add it to the list
        Game newGame = new Game(parent, gameName, world, spawnLocation, areaSize, durationHalf1, durationHalf2);
        this.activeGames.add(newGame);

        // draw game arena
        newGame.drawArena();

      return true;
    }

    public boolean StartGame(String gameName, ErrorMessage ErrorMsg)
    {
        // check if name already exists
        gameName = gameName.toLowerCase();

        Game game = null;

        for(Game g : this.activeGames)
        {
           if(g.getName().equals(gameName))
           {
               game = g;
           }
        }
        
        if(game == null)
        {
             ErrorMsg.SetMessage("A a game with this name does not exist.");
             return false;
        }

        if(!game.start(ErrorMsg))
        {
           return false;
        }

        return true;
    }

    public boolean CancelGame(String gameName, ErrorMessage ErrorMsg)
    {
        // check if name already exists
        gameName = gameName.toLowerCase();

        Game game = null;

        for(Game g : this.activeGames)
        {
           if(g.getName().equals(gameName))
           {
               game = g;
           }
        }

        if(game == null)
        {
             ErrorMsg.SetMessage("A a game with this name does not exist.");
             return false;
        }

        game.Cancel();

        this.activeGames.remove(game);

        return true;
    }


    public void PlayerJoined(Player player)
    {
        // if player is in a team, warp him to his base
        if (this.activeGames.size() < 1) {
            return;
        }

        for (Game g : this.activeGames) {
            g.CheckPlayerJoin(player);
        }
    }

    public void PlayerDisconnected(Player player)
    {
        // if player carries the magic lapis, spawn it at his last location
        if (this.activeGames.size() < 1) {
            return;
        }

        Iterator<Game> i = this.activeGames.iterator();
        while( i.hasNext()) {

            Game g  = i.next();

            g.CheckPlayerDisconnect(player);

            if(g.getStatus() == GameStatus.FINISHED)
                i.remove();
        }
        
    }

    public void PlayerDied(EntityDeathEvent event)
    {
        // if player carries the magic lapis, spawn it at his last location
        // warp the player back to his base
         if (this.activeGames.size() < 1) {
            return;
        }

        Iterator<Game> i = this.activeGames.iterator();
        while( i.hasNext()) {

            Game g = i.next();

            g.CheckPlayerDied(event);

            if(g.getStatus() == GameStatus.FINISHED)
                i.remove();
        }

    }
    

    public boolean IsMoveAllowed(PlayerMoveEvent event)
    {
        if(this.activeGames.size() < 1)
            return true;

        for(Game g : this.activeGames)
        {
           g.TimerTick();
        }
        
        for(Game g : this.activeGames)
        {
          if(!g.canPlayerMove(event))
              return false;
        }

        return true;
    }


    public boolean CanPlayerUseCommands(Player player)
    {
        // if in a team, block it
        if(this.activeGames.size() < 1)
            return true;

        for(Game g : this.activeGames)
        {
          if(!g.canPlayerUseCommands(player.getName()))
              return false;
        }

        return true;
    }

    public boolean BlockBlockInteraction(Player player, Block block)
    {
        // if player carries magic lapis, block it
        if(this.activeGames.size() < 1)
            return false;

        for(Game g : this.activeGames)
        {
          if(g.IsMagicLapisCarriedBy(player.getName()) != CarriesLapis.NONE)
              return true;
        }

        return false;
    }

    public boolean DropIsMagicLapis(Player player, ItemDrop drop)
    {
        // if it is magic lapis, block it and spawn it at that location as block
        if(this.activeGames.size() < 1)
            return false;


        Iterator<Game> i = this.activeGames.iterator();
        while( i.hasNext()) {

            Game g = i.next();

            boolean ret = g.CheckDropIsMagicLapis(player, drop);

            if(g.getStatus() == GameStatus.FINISHED)
                i.remove();

            if(ret)
                return true;
        }


        return false;
    }

    public void CheckBlockPlaceForMagicLapis(BlockPlaceEvent event)
    {
       // if it is magic lapis, save it
        if(this.activeGames.size() < 1)
            return;

        Iterator<Game> i = this.activeGames.iterator();
        while( i.hasNext()) {
            
            Game g = i.next();

            g.CheckBlockPlaceForMagicLapis(event);
            
            if(g.getStatus() == GameStatus.FINISHED)
                i.remove();
        }

    }

    public boolean CheckBrokenBlockForMagicLapis(BlockDamageEvent event)
    {
        // if it is magic lapis, put it in player inventory, cancel the broken block, and set block to air

        if(this.activeGames.size() < 1)
            return false;

        for(Game g : this.activeGames)
        {
          if(g.CheckBrokenBlockForMagicLapis(event))
              return true;
          if(g.CheckBrokenBlockForObsWall(event))
              return true;
        }

        return false;
    }

    public boolean IsMagicLapisBlock(Block block)
    {
        // return true, if block is a magic lapis block

        if(block == null)
            return false;
        
        if(this.activeGames.size() < 1)
            return false;

        for(Game g : this.activeGames)
        {
          if(g.IsMagicLapisBlock(block))
              return true;
        }
    
        return false;
    }
    
}
