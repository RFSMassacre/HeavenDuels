package com.github.rfsmassacre.heavenDuels;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    public static void removeDuel(UUID playerId)
    {
        Duel duel = getDuel(playerId);
        if (duel != null)
        {
            DUELS.remove(duel);
        }
    }

    public static Set<Duel> getDuels()
    {
        return new HashSet<>(DUELS);
    }

    private final Set<UUID> playerIds;

    public Duel(UUID... playerIds)
    {
        this.playerIds = Set.of(playerIds);
    }

    public boolean isDueling(UUID playerId)
    {
        return playerIds.contains(playerId);
    }
}
