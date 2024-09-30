package com.github.rfsmassacre.heavenduels;

import com.github.rfsmassacre.spigot.files.configs.Locale;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;

public class DuelListener implements Listener
{
    private final Locale locale;

    public DuelListener()
    {
        this.locale = HeavenDuels.getInstance().getLocale();
    }

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelFinalDamage(EntityDamageEvent event)
    {
        Player player = getPlayer(event.getEntity());
        if (player == null)
        {
            return;
        }

        Duel duel = Duel.getDuel(player.getUniqueId());
        if (duel == null)
        {
            return;
        }

        if (!event.isCancelled())
        {
            event.setCancelled(true);
        }

        if (player.getHealth() - event.getFinalDamage() > 0.0)
        {
            return;
        }

        Player opponent = duel.getOpponent(player);
        for (Player other : Bukkit.getOnlinePlayers())
        {
            locale.sendLocale(other, "duel.ended", "{winner}", opponent.getDisplayName(), "{loser}",
                    player.getDisplayName());
        }

        Duel.removeDuel(duel);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    public Player getPlayer(Entity entity)
    {
        switch (entity)
        {
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
