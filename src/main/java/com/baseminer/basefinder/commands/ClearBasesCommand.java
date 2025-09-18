package com.baseminer.basefinder.commands;

import com.baseminer.basefinder.modules.BaseFinderModule;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ClearBasesCommand extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(ClearBasesCommand.class);
    public ClearBasesCommand() {
        super("clear-bases", "Clears the list of reported bases.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Modules.get().get(BaseFinderModule.class).clearReportedBases();
            LOG.info("Cleared the list of reported bases.");
            info("Cleared the list of reported bases.");
            return SINGLE_SUCCESS;
        });
    }
}
