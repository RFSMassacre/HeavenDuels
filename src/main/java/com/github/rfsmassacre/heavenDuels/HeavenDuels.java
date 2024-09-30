package com.github.rfsmassacre.heavenDuels;

import com.github.rfsmassacre.spigot.files.configs.Configuration;
import com.github.rfsmassacre.spigot.files.configs.Locale;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class HeavenDuels extends JavaPlugin
{
    @Getter
    private static HeavenDuels instance;
    private Configuration configuration;
    private Locale locale;

    @Override
    public void onEnable()
    {
        instance = this;

        this.configuration = new Configuration(this, "", "config.yml");
        this.locale = new Locale(this, "", "locale.yml");
        getServer().getPluginManager().registerEvents(new DuelListener(), this);
        getCommand("duel").setExecutor(new DuelCommand());
    }

    @Override
    public void onDisable()
    {

    }
}
