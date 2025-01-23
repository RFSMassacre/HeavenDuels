package com.github.rfsmassacre.heavenduels;

import com.github.rfsmassacre.heavenduels.DuelCommand.DuelInvite;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenlibrary.paper.menu.DividerIcon;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import com.github.rfsmassacre.heavenlibrary.paper.menu.PageIcon;
import com.github.rfsmassacre.heavenlibrary.paper.menu.PlayerIcon;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelMenu extends Menu
{
    private final static int PAGE_LIMIT = 21;
    private final PaperConfiguration config;
    private final PaperLocale locale;

    public DuelMenu()
    {
        this(1);
    }

    public DuelMenu(int page)
    {
        super("&cInvite To Duel", 6, page);

        this.title += " (Page " + page + ")";
        this.config = HeavenDuels.getInstance().getConfiguration();
        this.locale = HeavenDuels.getInstance().getLocale();
    }

    @Override
    public void updateIcons(Player player)
    {
        if (Duel.getDuel(player.getUniqueId()) != null)
        {
            return;
        }

        ArrayList<Player> players = new ArrayList<>();
        for (Player otherPlayer : Bukkit.getOnlinePlayers())
        {
            if (player.equals(otherPlayer))
            {
                continue;
            }

            int radius = config.getInt("radius");
            if (!player.getWorld().equals(otherPlayer.getWorld()))
            {
                continue;
            }

            if (player.getLocation().distance(otherPlayer.getLocation()) > radius)
            {
                continue;
            }

            if (Duel.getDuel(otherPlayer.getUniqueId()) != null)
            {
                continue;
            }

            if (DuelInvite.getInvite(otherPlayer.getUniqueId()) != null)
            {
                continue;
            }

            players.add(otherPlayer);
        }

        List<List<Player>> pages = Lists.partition(players, PAGE_LIMIT);
        if (!pages.isEmpty())
        {
            for (Player otherPlayer : pages.get(page - 1))
            {
                for (int y = 2; y <= 4; y++)
                {
                    boolean placed = false;
                    for (int x = 2; x <= 8; x++)
                    {
                        PlayerDuelIcon icon = new PlayerDuelIcon(x, y, otherPlayer);
                        if (!slotTaken(icon.getSlot()))
                        {
                            addIcon(icon);
                            placed = true;
                            break;
                        }
                    }
                    if (placed)
                    {
                        break;
                    }
                }
            }
        }

        for (int x = 1; x <= 9; x++)
        {
            addIcon(new DividerIcon(x, 1, Material.RED_STAINED_GLASS_PANE));
        }
        for (int x = 1; x <= 9; x++)
        {
            addIcon(new DividerIcon(x, 5, Material.RED_STAINED_GLASS_PANE));
        }
        for (int y = 2; y <= 4; y++)
        {
            addIcon(new DividerIcon(1, y, Material.RED_STAINED_GLASS_PANE));
        }
        for (int y = 2; y <= 4; y++)
        {
            addIcon(new DividerIcon(9, y, Material.RED_STAINED_GLASS_PANE));
        }

        if (pages.size() > 1)
        {
            if (page + 1 < pages.size())
            {
                DuelMenu nextMenu = new DuelMenu(page + 1);
                addIcon(new PageIcon(9, 6, "&fNext Page", nextMenu));
            }

            if (page - 1 > 0)
            {
                DuelMenu lastMenu = new DuelMenu(page - 1);
                addIcon(new PageIcon(1, 6, "&fLast Page", lastMenu));
            }
        }
    }

    public class PlayerDuelIcon extends PlayerIcon
    {
        public PlayerDuelIcon(int x, int y, Player target)
        {
            super(x, y, target);
        }

        @Override
        public void onClick(Player player)
        {
            if (!target.isOnline())
            {
                return;
            }

            if (Duel.getDuel(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.self");
                player.closeInventory();
                return;
            }

            if (DuelInvite.getInvite(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.invite.already-sent");
                player.closeInventory();
                return;
            }

            if (player.equals(target))
            {
                locale.sendLocale(player, "duel.invite.self");
                return;
            }

            if (Duel.getDuel(target.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.target", "{target}",
                        target.getPlayer().getDisplayName());
                return;
            }

            if (DuelInvite.getInvite(target.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.invite.invited", "{target}",
                        target.getPlayer().getDisplayName());
                return;
            }

            DuelInvite.addInvite(new DuelInvite(player.getUniqueId(), target.getUniqueId()));
            locale.sendLocale(player, "duel.invite.sent", "{target}", target.getPlayer().getDisplayName());
            locale.sendLocale(target.getPlayer(), "duel.invite.received", "{player}",
                    player.getDisplayName());
            player.closeInventory();
        }
    }
}
