package com.github.rfsmassacre.heavenduels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Duel
{
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

    public static void removeDuel(UUID playerId)
    {
        Duel duel = getDuel(playerId);
        if (duel != null)
        {
            removeDuel(duel);
        }
    }

    public static Set<Duel> getDuels()
    {
        return new HashSet<>(DUELS);
    }

    private final List<UUID> playerIds;

    public Duel(UUID... playerIds)
    {
        this.playerIds = List.of(playerIds);
    }

    public boolean isDueling(UUID playerId)
    {
        return playerIds.contains(playerId);
    }

    public List<UUID> getOpponents(Player player)
    {
        List<UUID> opponents = new ArrayList<>(playerIds);
        opponents.removeIf((playerId) -> playerId.equals(player.getUniqueId()));
        return opponents;
    }

    public Player getOpponent(Player player)
    {
        return Bukkit.getPlayer(getOpponents(player).getFirst());
    }
}
