package com.github.rfsmassacre.heavenDuels;

import com.github.rfsmassacre.spigot.commands.SpigotCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelCommand extends SpigotCommand
{
    private record DuelInvite(UUID challengerId, UUID targetId)
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
                INVITES.remove(invite);
            }
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
    }

    public DuelCommand()
    {
        super(HeavenDuels.getInstance().getLocale(), "heavenduels", "duel");

        addSubCommand(new InviteCommand());
        addSubCommand(new AcceptCommand());
    }

    @Override
    protected void onFail(CommandSender sender)
    {
        locale.sendLocale(sender, "invalid.no-perm");
    }

    @Override
    protected void onInvalidArgs(CommandSender sender)
    {
        locale.sendLocale(sender, "invalid.invalid-args");
    }

    private class InviteCommand extends SubCommand
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
                return;
            }

            if (Duel.getDuel(player.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.self");
                return;
            }

            if (args.length < 2)
            {
                locale.sendLocale(player, "invalid.invalid-sub-args", "{command}", commandName, "{args}",
                        "<player>");
                return;
            }

            String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null)
            {
                locale.sendLocale(player, "invalid.no-player", "{player}", playerName);
                return;
            }

            if (Duel.getDuel(target.getUniqueId()) != null)
            {
                locale.sendLocale(player, "duel.in-duel.target", "{target}", target.getDisplayName());
                return;
            }

            DuelInvite.addInvite(new DuelInvite(player.getUniqueId(), target.getUniqueId()));
            locale.sendLocale(player, "duel.invite.sent", "{target}", target.getDisplayName());
            locale.sendLocale(target, "duel.invite.received", "{player}", player.getDisplayName());
        }

        @Override
        protected List<String> onTabComplete(CommandSender sender, String[] args)
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

    private class AcceptCommand extends SubCommand
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
                return;
            }

            DuelInvite invite = DuelInvite.getInvite(player.getUniqueId());
            if (invite == null)
            {
                locale.sendLocale(player, "duel.invite.no-invite");
                return;
            }


            Duel.addDuel(new Duel(invite.challengerId, invite.targetId));
            for (Player other : Bukkit.getOnlinePlayers())
            {
                locale.sendLocale(other, "invite.started", "{player}",
                        invite.getChallenger().getDisplayName(), "{target}", invite.getTarget().getDisplayName());
            }
        }

        @Override
        protected List<String> onTabComplete(CommandSender sender, String[] args)
        {
            return Collections.emptyList();
        }
    }
}
