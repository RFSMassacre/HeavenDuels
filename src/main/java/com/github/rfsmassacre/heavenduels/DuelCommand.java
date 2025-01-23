package com.github.rfsmassacre.heavenduels;

import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelCommand extends PaperCommand
{
    public record DuelInvite(UUID challengerId, UUID targetId)
    {
        private static final Set<DuelInvite> INVITES = new HashSet<>();

        public static DuelInvite getInvite(UUID playerId)
        {
            for (DuelInvite invite : INVITES)
            {
               if (invite.challengerId.equals(playerId) || invite.targetId.equals(playerId))
               {
                   return invite;
               }
            }

            return null;
        }

        public static void addInvite(DuelInvite invite)
        {
            INVITES.add(invite);
        }

        public static void removeInvite(UUID playerId)
        {
            DuelInvite invite = getInvite(playerId);
            if (invite != null)
            {
                removeInvite(invite);
            }
        }

        public static void removeInvite(DuelInvite invite)
        {
            INVITES.remove(invite);
        }

        public static Set<DuelInvite> getInvites()
        {
            return new HashSet<>(INVITES);
        }

        public Player getChallenger()
        {
            return Bukkit.getPlayer(challengerId);
        }

        public Player getTarget()
        {
            return Bukkit.getPlayer(targetId);
        }

        public Player getOpponent(Player player)
        {
            if (!player.getUniqueId().equals(challengerId))
            {
                return getChallenger();
            }
            else
            {
                return getTarget();
            }
        }
    }

    public DuelCommand()
    {
        super(HeavenDuels.getInstance(), "duel");
    }

    private class MenuCommand extends PaperSubCommand
    {
        public MenuCommand()
        {
            super("menu");
        }

        @Override
        protected void onRun(CommandSender sender, String[] strings)
        {
            if (!(sender instanceof Player player))
            {
                onConsole(sender);
                return;
            }

            DuelMenu menu = new DuelMenu();
            Menu.addView(player.getUniqueId(), menu);
            player.openInventory(menu.createInventory(player));
            playSound(player, SoundKey.SUCCESS);
        }
    }

    private class InviteCommand extends PaperSubCommand
    {
        public InviteCommand()
        {
            super("invite");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            if (!(sender instanceof Player player))
            {
                locale.sendLocale(sender, "invalid.console");
                return;
            }

            if (Duel.getDuel(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.self");
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            if (DuelInvite.getInvite(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.invite.already-sent");
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }

            if (args.length < 2)
            {
                locale.sendLocale(player, "invalid.invalid-args", "{command}", commandName, "{args}",
                        name + " <player>");
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null)
            {
                locale.sendLocale(player, "invalid.no-player", "{player}", playerName);
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            if (player.equals(target))
            {
                locale.sendLocale(player, "duel.invite.self");
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }


            if (DuelInvite.getInvite(target.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.invite.invited", "{target}", target.getDisplayName());
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }

            if (Duel.getDuel(target.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.target", "{target}", target.getDisplayName());
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }

            DuelInvite.addInvite(new DuelInvite(player.getUniqueId(), target.getUniqueId()));
            locale.sendLocale(player, "duel.invite.sent", "{target}", target.getDisplayName());
            locale.sendLocale(target, "duel.invite.received", "{player}", player.getDisplayName());
            playSound(sender, SoundKey.SUCCESS);
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args)
        {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 2)
            {
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    if (!sender.equals(player))
                    {
                        suggestions.add(player.getName());
                    }
                }
            }

            return suggestions;
        }
    }

    private class AcceptCommand extends PaperSubCommand
    {
        public AcceptCommand()
        {
            super("accept");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            if (!(sender instanceof Player player))
            {
                onConsole(sender);
                return;
            }

            if (Duel.getDuel(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.self");
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            DuelInvite invite = DuelInvite.getInvite(player.getUniqueId());
            if (invite == null)
            {
                locale.sendLocale(player, "duel.invite.no-invite");
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            int radius = config.getInt("radius");
            Player challenger = invite.getChallenger();
            if (challenger == null || challenger.equals(player))
            {
                locale.sendLocale(player, "duel.invite.no-invite");
                DuelInvite.removeInvite(invite);
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            if (!player.getWorld().equals(challenger.getWorld()) ||
                    player.getLocation().distance(challenger.getLocation()) > radius)
            {
                locale.sendLocale(player, "duel.invite.out-of-radius", "{player}",
                        challenger.getDisplayName());
                playSound(player, SoundKey.INCOMPLETE);
                return;
            }

            DuelInvite.removeInvite(invite);
            Duel.addDuel(new Duel(player, challenger));
            for (Player other : Bukkit.getOnlinePlayers())
            {
                locale.sendLocale(other, "duel.started", "{player}",
                        invite.getChallenger().getDisplayName(), "{target}", invite.getTarget().getDisplayName());
            }

            playSound(sender, SoundKey.SUCCESS);
        }
    }

    private class DenyCommand extends PaperSubCommand
    {
        public DenyCommand()
        {
            super("deny");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            if (!(sender instanceof Player player))
            {
                return;
            }

            DuelInvite invite = DuelInvite.getInvite(player.getUniqueId());
            if (invite == null)
            {
                locale.sendLocale(player, "duel.invite.no-invite");
                playSound(sender, SoundKey.INCOMPLETE);
                return;
            }

            DuelInvite.removeInvite(invite);
            locale.sendLocale(invite.getTarget(), "duel.invite.denied.self", "{player}",
                    invite.getChallenger().getDisplayName());
            locale.sendLocale(invite.getChallenger(), "duel.invite.denied.target", "{player}",
                    invite.getTarget().getDisplayName());
            playSound(sender, SoundKey.SUCCESS);
        }
    }

    private class ReloadCommand extends PaperSubCommand
    {
        public ReloadCommand()
        {
            super("reload", "heavenduels.admin");
        }

        @Override
        protected void onRun(CommandSender sender, String[] args)
        {
            config.reload();
            locale.reload();
            locale.sendLocale(sender, "reloaded");
            playSound(sender, SoundKey.SUCCESS);
        }
    }
}
