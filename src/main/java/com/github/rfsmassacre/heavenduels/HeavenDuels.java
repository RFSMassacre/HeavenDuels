package com.github.rfsmassacre.heavenduels;

import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import lombok.Getter;

@Getter
public final class HeavenDuels extends HeavenPaperPlugin
{
    @Getter
    private static HeavenDuels instance;

    @Override
    public void onEnable()
    {
        instance = this;
        addYamlManager(new PaperConfiguration(this, "", "config.yml", true));
        addYamlManager(new PaperLocale(this, "", "locale.yml", true));
        getServer().getPluginManager().registerEvents(new DuelListener(), this);
        getCommand("duel").setExecutor(new DuelCommand());
    }
}
