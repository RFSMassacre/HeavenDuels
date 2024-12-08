package com.github.rfsmassacre.heavenduels;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.*;

public class Duel
{
    private record Health(UUID playerId, double health, int food, float saturation)
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
                    player.getFoodLevel(), player.getSaturation()));
        }
    }

    public double originalHealth(UUID playerId)
    {
        Health health = playerHealths.get(playerId);
        if (health == null)
        {
            return 0.0;
        }

        return health.health;
    }

    public int originalFood(UUID playerId)
    {
        Health health = playerHealths.get(playerId);
        if (health == null)
        {
            return 0;
        }

        return health.food;
    }

    public float originalSaturation(UUID playerId)
    {
        Health health = playerHealths.get(playerId);
        if (health == null)
        {
            return 0.0F;
        }

        return health.saturation;
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
    }

    public boolean isDueling(UUID playerId)
    {
        return playerHealths.containsKey(playerId);
    }

    public List<UUID> getOpponents(Player player)
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
