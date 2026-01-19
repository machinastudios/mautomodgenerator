package com.machina.mautomodgenerator.commands;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.machina.mautomodgenerator.Main;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;

/**
 * Main mod generator command
 */
public class CommandHandler extends CommandBase {
    public CommandHandler() {
        super("mautomodgenerator", "mautomodgenerator.command.desc");

        // Add aliases
        addAliases("mamg");

        // Add reload subcommand
        addSubCommand(new RebuildCommand());
    }

    @Override
    public void executeSync(@Nonnull CommandContext context) {
        // If no subcommand, show help
        context.sendMessage(Message.translation("mautomodgenerator.command.usage"));
    }
}

/**
 * Rebuild subcommand - rebuilds the mod ZIP file
 * Usage: /mautomodgenerator rebuild
 */
class RebuildCommand extends CommandBase {
    public RebuildCommand() {
        super("rebuild", "mautomodgenerator.command.rebuild.desc");

        requirePermission("mautomodgenerator.command.rebuild.permission");
    }

    @Override
    public void executeSync(@Nonnull CommandContext context) {
        // Build the mod manifest
        Main.INSTANCE.buildMod();

        // Send success message
        context.sendMessage(Message.translation("mautomodgenerator.command.rebuild.success"));
    }
}