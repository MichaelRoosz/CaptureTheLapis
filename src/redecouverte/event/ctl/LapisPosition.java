package redecouverte.event.ctl;

import java.util.ArrayList;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.*;


public class LapisPosition {

    private boolean isCarriedByPlayer;
    private Player player;
    private int x,y,z;

    public LapisPosition(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = null;

        this.isCarriedByPlayer = false;
    }

    public void setCoords(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = null;
        
        this.isCarriedByPlayer = false;
    }

    public void setPlayer(Player player)
    {
       this.player = player;

       this.isCarriedByPlayer = true;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }
    
    public boolean coordsEqual(int x, int y, int z)
    {
        return !this.isCarriedByPlayer && this.x == x && this.y == y && this.z == z;
    }

    public Player getPlayer()
    {
        return this.player;
    }

    public boolean isCarriedByPlayer()
    {
        return this.isCarriedByPlayer;
    }

}
