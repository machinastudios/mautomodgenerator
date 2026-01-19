package com.machina.mautomodgenerator;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * Dummy main class for the mod
 * It's used to make the mod a valid plugin
 * and to be able to reload the mod
 * without having to restart the server
 * 
 * Does nothing, just to make the mod a valid plugin
 */
public class DummyMain extends JavaPlugin {
    public DummyMain(@Nonnull JavaPluginInit init) {
        super(init);
    }
}
