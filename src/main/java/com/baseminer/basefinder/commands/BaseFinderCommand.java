package com.baseminer.basefinder.commands;

import com.baseminer.basefinder.utils.ElytraController;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BaseFinderCommand extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(BaseFinderCommand.class);
    public BaseFinderCommand() {
        super("basefinder", "Starts the base finding process.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("x1", IntegerArgumentType.integer())
            .then(argument("z1", IntegerArgumentType.integer())
                .then(argument("x2", IntegerArgumentType.integer())
                    .then(argument("z2", IntegerArgumentType.integer())
                        .executes(context -> {
                            int x1 = IntegerArgumentType.getInteger(context, "x1");
                            int z1 = IntegerArgumentType.getInteger(context, "z1");
                            int x2 = IntegerArgumentType.getInteger(context, "x2");
                            int z2 = IntegerArgumentType.getInteger(context, "z2");

                            String message = String.format("Starting base finding process from (%d, %d) to (%d, %d)", x1, z1, x2, z2);
                            LOG.info(message);
                            info(message);
                            ElytraController.start(x1, z1, x2, z2, 100);
                            return SINGLE_SUCCESS;
                        })
                    )
                )
            )
        );
    }
}
