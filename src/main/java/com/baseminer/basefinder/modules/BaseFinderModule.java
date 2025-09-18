package com.baseminer.basefinder.modules;

import com.baseminer.basefinder.BaseFinder;
import com.baseminer.basefinder.utils.Config;
import com.baseminer.basefinder.utils.DiscordEmbed;
import com.baseminer.basefinder.utils.DiscordWebhook;
import com.baseminer.basefinder.utils.ElytraController;
import com.baseminer.basefinder.utils.WorldScanner;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseFinderModule extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final List<PlayerEntity> playersInRadius = new ArrayList<>();
    private final List<BlockPos> reportedBases = new ArrayList<>();

    private final Setting<String> discordWebhookUrl = sgGeneral.add(new StringSetting.Builder()
        .name("discord-webhook-url")
        .description("The Discord webhook URL to send notifications to.")
        .defaultValue(Config.discordWebhookUrl)
        .onChanged(v -> {
            Config.discordWebhookUrl = v;
            Config.save();
        })
        .build()
    );

    private final Setting<Integer> blockDetectionThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("block-detection-threshold")
        .description("The number of valuable blocks to find before a base is detected.")
        .defaultValue(Config.blockDetectionThreshold)
        .onChanged(v -> {
            Config.blockDetectionThreshold = v;
            Config.save();
        })
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("The radius to scan for valuable blocks.")
        .defaultValue(Config.scanRadius)
        .onChanged(v -> {
            Config.scanRadius = v;
            Config.save();
        })
        .build()
    );

    private final Setting<Boolean> playerDetection = sgGeneral.add(new BoolSetting.Builder()
        .name("player-detection")
        .description("Whether to notify when a player is detected.")
        .defaultValue(Config.playerDetection)
        .onChanged(v -> {
            Config.playerDetection = v;
            Config.save();
        })
        .build()
    );

    private final Setting<Boolean> notifyOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-on-death")
        .description("Whether to notify when you die.")
        .defaultValue(Config.notifyOnDeath)
        .onChanged(v -> {
            Config.notifyOnDeath = v;
            Config.save();
        })
        .build()
    );


    public BaseFinderModule() {
        super(BaseFinder.CATEGORY, "base-finder", "Automatically finds bases by flying around and scanning for valuable blocks.");
    }

    @Override
    public void onActivate() {
        playersInRadius.clear();
    }

    @Override
    public void onDeactivate() {
        ElytraController.stop();
    }

    public void clearReportedBases() {
        reportedBases.clear();
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();
        ElytraController.onChatMessage(message);

        if (notifyOnDeath.get() && mc.player != null && message.contains(mc.player.getName().getString()) && message.contains("was killed")) {
            DiscordEmbed embed = new DiscordEmbed("Bot Died!", "Coordinates: " + mc.player.getBlockPos().toShortString(), 0xFF0000);
            DiscordWebhook.sendMessage("@everyone", embed);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Base scanning
        List<BlockPos> valuableBlocks = WorldScanner.scanForValuableBlocks(mc.player.getPos(), scanRadius.get());

        if (valuableBlocks.size() >= blockDetectionThreshold.get()) {
            BlockPos basePos = valuableBlocks.get(0);
            boolean alreadyReported = false;
            for (BlockPos reportedBase : reportedBases) {
                if (reportedBase.isWithinDistance(basePos, 100)) { // 100 block radius to consider it the same base
                    alreadyReported = true;
                    break;
                }
            }

            if (!alreadyReported) {
                reportedBases.add(basePos);
                String coords = basePos.toShortString();
                double volume = WorldScanner.getBoundingBoxVolume(valuableBlocks);
                double density = valuableBlocks.size() / volume;
                String rating;
                if (density > 0.5) {
                    rating = "Very High Density";
                } else if (density > 0.2) {
                    rating = "High Density";
                } else if (density > 0.1) {
                    rating = "Medium Density";
                } else if (density > 0.05) {
                    rating = "Low Density";
                } else {
                    rating = "Very Low Density";
                }

                Map<Block, Integer> counts = WorldScanner.countBlocks(valuableBlocks);
                StringBuilder containerList = new StringBuilder();
                for (Map.Entry<Block, Integer> entry : counts.entrySet()) {
                    containerList.append(entry.getValue()).append("x ").append(entry.getKey().getName().getString()).append("\\n");
                }

                String description = "Coordinates: " + coords + "\\n" +
                                     "Found " + valuableBlocks.size() + " valuable blocks.\\n" +
                                     "Volume: " + String.format("%.2f", volume) + " blocks\\n" +
                                     "Density: " + String.format("%.4f", density) + "\\n" +
                                     "Rating: " + rating + "\\n\\n" +
                                     "Container List:\\n" + containerList.toString();

                DiscordEmbed embed = new DiscordEmbed("Base Found!", description, 0x00FF00);
                DiscordWebhook.sendMessage("@everyone", embed);
                info("Base found at: " + coords);
            }
        }

        // Player detection
        if (playerDetection.get()) {
            List<PlayerEntity> currentPlayersInRadius = new ArrayList<>();
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) {
                    continue;
                }

                if (mc.player.distanceTo(player) < 100) {
                    currentPlayersInRadius.add(player);
                    if (!playersInRadius.contains(player)) {
                        DiscordEmbed embed = new DiscordEmbed("Player Detected!", "Player: " + player.getName().getString() + "\\nCoordinates: " + player.getBlockPos().toShortString(), 0xFFFF00);
                        DiscordWebhook.sendMessage("", embed);
                    }
                }
            }
            playersInRadius.clear();
            playersInRadius.addAll(currentPlayersInRadius);
        }
    }

}
