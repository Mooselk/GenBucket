package me.kate.genbuckets;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.kate.genbuckets.commands.GenBucketAdminCommand;
import me.kate.genbuckets.commands.GenBucketCommand;
import me.kate.genbuckets.hooks.HookUtils;
import me.kate.genbuckets.listeners.PlayerListener;
import me.kate.genbuckets.utils.BucketManager;
import me.kate.genbuckets.utils.ConfigValues;
import me.kate.genbuckets.utils.Utils;

public class Main extends JavaPlugin {
	
	private ConfigValues configValues;
    private Utils utils;
    private HookUtils hookUtils;
    private BucketManager bucketManager;
    private int minecraftVersion = -1;

    @Override
    public void onEnable() {
    	
        if (minecraftVersion == -1) {
            String mcVersion = Bukkit.getBukkitVersion().split(Pattern.quote("-"))[0].split(Pattern.quote("."))[1];
            if (mcVersion.contains(".")) {
                mcVersion = mcVersion.split(Pattern.quote("."))[0];
            }
            minecraftVersion = Integer.valueOf(mcVersion);
        }
        
        bucketManager = new BucketManager();
        utils = new Utils(this);
        configValues = new ConfigValues(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("genbucket").setExecutor(new GenBucketCommand(this));
        GenBucketAdminCommand gbaCommand = new GenBucketAdminCommand(this);
        getCommand("genbucketadmin").setExecutor(gbaCommand);
        getCommand("genbucketadmin").setTabCompleter(gbaCommand);
        saveDefaultConfig();
        hookUtils = new HookUtils(this);
        utils.registerRecipes();
        utils.updateConfig();
        configValues.loadBuckets();
        
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }

    public Utils getUtils() {
        return utils;
    }

    public HookUtils getHookUtils() {
        return hookUtils;
    }

    public BucketManager getBucketManager() {
        return bucketManager;
    }

    // using mc 1.8 to 1.12
    public boolean serverIsBeforeFlattening() {
        return minecraftVersion < 13;
    }

    // using mc 1.8
    public boolean serverIsAfterOffhand() {
        return minecraftVersion > 8;
    }
}
