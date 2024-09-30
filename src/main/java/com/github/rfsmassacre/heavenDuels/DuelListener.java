package com.github.rfsmassacre.heavenDuels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;

public class DuelListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelDamage(EntityDamageByEntityEvent event)
    {
        Player defender = getPlayer(event.getEntity());
        Player attacker = getPlayer(event.getDamager());
        if (defender == null || attacker == null)
        {
            return;
        }

        Duel duel = Duel.getDuel(attacker.getUniqueId());
        if (duel == null)
        {
            return;
        }

        if (event.isCancelled() && duel.isDueling(defender.getUniqueId()))
        {
            event.setCancelled(false);
        }
    }

    public Player getPlayer(Entity entity)
    {
        switch (entity)
        {
            case null ->
            {
                return null;
            }

            //Filter through the owner possibilities
            case Player player ->
            {
                if (player.hasMetadata("NPC"))
                {
                    return null;
                }
                else
                {
                    return player;
                }
            }
            case Projectile projectile ->
            {
                if (projectile.getShooter() instanceof Player player)
                {
                    return player;
                }
            }
            default ->
            {
                if (entity.hasMetadata("Player"))
                {
                    MetadataValue value = entity.getMetadata("Player").getFirst();
                    for (Player player : Bukkit.getOnlinePlayers())
                    {
                        if (value.asString().contains(player.getName()))
                        {
                            return player;
                        }
                    }
                }
            }
        }

        return null;
    }
}
