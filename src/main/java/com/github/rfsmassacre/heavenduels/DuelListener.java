package com.github.rfsmassacre.heavenduels;

import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;

public class DuelListener implements Listener
{
    private final PaperConfiguration config;
    private final PaperLocale locale;

    public DuelListener()
    {
        this.config = HeavenDuels.getInstance().getConfiguration();
        this.locale = HeavenDuels.getInstance().getLocale();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDuelDamage(EntityDamageEvent event)
    {
        Player defender = getPlayer(event.getEntity());
        if (defender == null)
        {
            return;
        }

        Duel duel = Duel.getDuel(defender.getUniqueId());
        if (duel == null)
        {
            return;
        }

        Player opponent = duel.getOpponent(defender);
        if (defender.getHealth() - event.getFinalDamage() <= 0.0)
        {
            for (Player other : Bukkit.getOnlinePlayers())
            {
                locale.sendLocale(other, "duel.ended", "{winner}", opponent.getDisplayName(), "{loser}",
                        defender.getDisplayName());
            }

            int fadeIn = config.getInt("times.fade-in");
            int stay = config.getInt("times.stay");
            int fadeOut = config.getInt("times.fade-out");
            String title = locale.getMessage("duel.won.title", false);
            String subtitle = locale.getMessage("duel.won.subtitle", false);
            locale.sendTitleMessage(opponent, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    defender.getDisplayName());
            title = locale.getMessage("duel.lost.title", false);
            subtitle = locale.getMessage("duel.lost.subtitle", false);
            locale.sendTitleMessage(defender, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    opponent.getDisplayName());

            Duel.removeDuel(duel);
            duel.restoreHealth(defender);
            duel.restoreHealth(opponent);
            event.setCancelled(true);
            defender.playEffect(EntityEffect.TOTEM_RESURRECT);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent entityEvent)
        {
            Player attacker = getPlayer(entityEvent.getDamager());
            if (attacker != null && !opponent.getUniqueId().equals(attacker.getUniqueId()))
            {
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFleeTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        Duel duel = Duel.getDuel(player.getUniqueId());
        if (duel == null)
        {
            return;
        }

        int radius = config.getInt("radius");
        Player opponent = duel.getOpponent(player);
        Location to = event.getTo();
        if (!opponent.getWorld().equals(to.getWorld()) || opponent.getLocation().distance(to) > radius)
        {
            Duel.removeDuel(duel);
            duel.restoreHealth(player);
            duel.restoreHealth(opponent);
            for (Player other : Bukkit.getOnlinePlayers())
            {
                locale.sendLocale(other, "duel.retreated", "{winner}", opponent.getDisplayName(), "{loser}",
                        player.getDisplayName());
            }

            int fadeIn = config.getInt("times.fade-in");
            int stay = config.getInt("times.stay");
            int fadeOut = config.getInt("times.fade-out");
            String title = locale.getMessage("duel.won.title", false);
            String subtitle = locale.getMessage("duel.won.retreated", false);
            locale.sendTitleMessage(opponent, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    player.getDisplayName());
            title = locale.getMessage("duel.lost.title", false);
            subtitle = locale.getMessage("duel.lost.retreated", false);
            locale.sendTitleMessage(player, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    opponent.getDisplayName());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFleeMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        Duel duel = Duel.getDuel(player.getUniqueId());
        if (duel == null)
        {
            return;
        }

        int radius = config.getInt("radius");
        Player opponent = duel.getOpponent(player);
        Location to = event.getTo();
        if (!opponent.getWorld().equals(to.getWorld()) || opponent.getLocation().distance(to) > radius)
        {
            Duel.removeDuel(duel);
            duel.restoreHealth(player);
            duel.restoreHealth(opponent);
            for (Player other : Bukkit.getOnlinePlayers())
            {
                locale.sendLocale(other, "duel.retreated", "{winner}", opponent.getDisplayName(), "{loser}",
                        player.getDisplayName());
            }

            int fadeIn = config.getInt("times.fade-in");
            int stay = config.getInt("times.stay");
            int fadeOut = config.getInt("times.fade-out");
            String title = locale.getMessage("duel.won.title", false);
            String subtitle = locale.getMessage("duel.won.retreated", false);
            locale.sendTitleMessage(opponent, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    player.getDisplayName());
            title = locale.getMessage("duel.lost.title", false);
            subtitle = locale.getMessage("duel.lost.retreated", false);
            locale.sendTitleMessage(player, fadeIn, stay, fadeOut, title, subtitle, "{player}",
                    opponent.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        DuelCommand.DuelInvite invite = DuelCommand.DuelInvite.getInvite(player.getUniqueId());
        if (invite != null)
        {
            Player opponent = invite.getOpponent(player);
            locale.sendLocale(opponent, "duel.invite.denied.quit", "{player}", player.getDisplayName());
            DuelCommand.DuelInvite.removeInvite(invite);
        }
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
            case Tameable tameable ->
            {
                if (tameable.getOwner() instanceof Player player)
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
