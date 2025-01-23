package com.github.rfsmassacre.heavenduels;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class Duel
{
    private record Health(UUID playerId, double health, int food, float saturation, int fireTick,
                          Collection<PotionEffect> potionEffects)
    {

    }

    private static final Set<Duel> DUELS = new HashSet<>();

    public static Duel getDuel(UUID playerId)
    {
        for (Duel duel : DUELS)
        {
            if (duel.isDueling(playerId))
            {
                return duel;
            }
        }

        return null;
    }

    public static void addDuel(Duel duel)
    {
        DUELS.add(duel);
    }

    public static void removeDuel(Duel duel)
    {
        DUELS.remove(duel);
    }

    public static Set<Duel> getDuels()
    {
        return new HashSet<>(DUELS);
    }

    private final Map<UUID, Health> playerHealths;

    public Duel(Player... players)
    {
        this.playerHealths = new HashMap<>();
        for (Player player : players)
        {
            playerHealths.put(player.getUniqueId(), new Health(player.getUniqueId(), player.getHealth(),
                    player.getFoodLevel(), player.getSaturation(), player.getFireTicks(),
                    player.getActivePotionEffects()));
        }
    }

    public void restoreHealth(Player player)
    {
        Health health = playerHealths.get(player.getUniqueId());
        if (health == null)
        {
            return;
        }

        player.setHealth(Math.min(health.health, player.getAttribute(Attribute.MAX_HEALTH).getValue()));
        player.setFoodLevel(Math.min(health.food, 20));
        player.setSaturation(Math.min(health.saturation, 20.0F));
        player.setFireTicks(Math.max(0, health.fireTick));
        player.clearActivePotionEffects();
        player.addPotionEffects(health.potionEffects);
    }

    public boolean isDueling(UUID playerId)
    {
        return playerHealths.containsKey(playerId);
    }

    private List<UUID> getOpponents(Player player)
    {
        List<UUID> opponents = new ArrayList<>(playerHealths.keySet());
        opponents.removeIf((playerId) -> playerId.equals(player.getUniqueId()));
        return opponents;
    }

    public Player getOpponent(Player player)
    {
        return Bukkit.getPlayer(getOpponents(player).getFirst());
    }
}
