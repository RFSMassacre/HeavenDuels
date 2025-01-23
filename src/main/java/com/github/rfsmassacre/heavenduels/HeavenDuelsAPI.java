package com.github.rfsmassacre.heavenduels;

import java.util.UUID;

public class HeavenDuelsAPI
{
    public static boolean isDueling(UUID playerOne, UUID playerTwo)
    {
        if (HeavenDuels.getInstance() == null)
        {
            return false;
        }

        Duel duel = Duel.getDuel(playerOne);
        if (duel != null)
        {
            return duel.isDueling(playerTwo);
        }

        return false;
    }

    public static boolean isDueling(UUID playerId)
    {
        if (HeavenDuels.getInstance() == null)
        {
            return false;
        }

        return Duel.getDuel(playerId) != null;
    }
}
