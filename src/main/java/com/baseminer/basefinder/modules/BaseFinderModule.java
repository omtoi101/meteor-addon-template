package com.baseminer.basefinder.modules;

import com.baseminer.basefinder.BaseFinder;
import com.baseminer.basefinder.events.PlayerDeathEvent;
import com.baseminer.basefinder.utils.Config;
import com.baseminer.basefinder.utils.DiscordEmbed;
import com.baseminer.basefinder.utils.DiscordWebhook;
import com.baseminer.basefinder.utils.ElytraController;
import com.baseminer.basefinder.utils.WorldScanner;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseFinderModule extends Module {
    // Settings
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

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

    private final Setting<Integer> flightAltitude = sgGeneral.add(new IntSetting.Builder()
        .name("flight-altitude")
        .description("The altitude to fly at.")
        .defaultValue(Config.flightAltitude)
        .onChanged(v -> {
            Config.flightAltitude = v;
            Config.save();
        })
        .build()
    );

    private final Setting<Integer> scanInterval = sgGeneral.add(new IntSetting.Builder()
        .name("scan-interval")
        .description("The interval in ticks between scans.")
        .defaultValue(Config.scanInterval)
        .min(1)
        .sliderMax(100)
        .onChanged(v -> {
            Config.scanInterval = v;
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

    // State
    private final Map<PlayerEntity, Long> reportedPlayers = new HashMap<>();
    private final List<BlockPos> reportedBases = new ArrayList<>();
    private final List<BlockPos> valuableBlocksInRange = new ArrayList<>();
    private int tickCounter = 0;


    public BaseFinderModule() {
        super(BaseFinder.CATEGORY, "base-finder", "Automatically finds bases by flying around and scanning for valuable blocks.");
    }

    @Override
    public void onActivate() {
        reportedPlayers.clear();
    }

    @Override
    public void onDeactivate() {
        ElytraController.stop();
    }

    public void clearReportedBases() {
        reportedBases.clear();
    }

    public void clearReportedPlayers() {
        reportedPlayers.clear();
    }

    /**
     * Scans for valuable blocks in the render thread.
     * This is done on the render thread to leverage the game's existing block entity iteration, which is more performant.
     * The scan is throttled by the scanInterval setting to avoid performance issues.
     */
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (tickCounter % scanInterval.get() != 0) return;

        valuableBlocksInRange.clear();
        mc.world.getBlockEntities().forEach(blockEntity -> {
            if (Config.valuableBlocks.contains(blockEntity.getCachedState().getBlock())) {
                if (blockEntity.getPos().isWithinDistance(mc.player.getPos(), scanRadius.get())) {
                    valuableBlocksInRange.add(blockEntity.getPos());
                }
            }
        });
    }

    /**
     * Handles chat messages for elytra pilot and death detection.
     */
    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        // Empty for now
    }

    /**
     * Handles player death events.
     */
    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (notifyOnDeath.get() && event.player == mc.player) {
            DiscordEmbed embed = new DiscordEmbed("Bot Died!", "Coordinates: " + event.player.getBlockPos().toShortString(), 0xFF0000);
            DiscordWebhook.sendMessage("@everyone", embed);
        }
    }

    /**
     * Main tick loop for the module.
     * Handles base detection and player detection.
     */
    @EventHandler
    private void onTick(TickEvent.Post event) {
        tickCounter++;
        ElytraController.onTick();
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Base scanning logic
        if (valuableBlocksInRange.size() >= blockDetectionThreshold.get()) {
            BlockPos basePos = valuableBlocksInRange.get(0);
            boolean alreadyReported = false;
            for (BlockPos reportedBase : reportedBases) {
                if (reportedBase.isWithinDistance(basePos, 100)) {
                    alreadyReported = true;
                    break;
                }
            }

            if (!alreadyReported) {
                reportedBases.add(basePos);
                String coords = basePos.toShortString();
                double volume = WorldScanner.getBoundingBoxVolume(valuableBlocksInRange);
                double density = valuableBlocksInRange.size() / volume;
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

                Map<Block, Integer> counts = WorldScanner.countBlocks(valuableBlocksInRange);
                StringBuilder containerList = new StringBuilder();
                for (Map.Entry<Block, Integer> entry : counts.entrySet()) {
                    containerList.append(entry.getValue()).append("x ").append(entry.getKey().getName().getString()).append("\n");
                }

                String description = "Coordinates: " + coords + "\n" +
                                     "Found " + valuableBlocksInRange.size() + " valuable blocks.\n" +
                                     "Volume: " + String.format("%.2f", volume) + " blocks\n" +
                                     "Density: " + String.format("%.4f", density) + "\n" +
                                     "Rating: " + rating + "\n\n" +
                                     "Container List:\n" + containerList.toString();

                DiscordEmbed embed = new DiscordEmbed("Base Found!", description, 0x00FF00);
                DiscordWebhook.sendMessage("@everyone", embed);
                info("Base found at: " + coords);
            }
        }

        // Player detection logic
        if (playerDetection.get()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) {
                    continue;
                }

                if (mc.player.distanceTo(player) < 100) {
                    if (!reportedPlayers.containsKey(player) || System.currentTimeMillis() - reportedPlayers.get(player) > 300000) { // 5 minute cooldown
                        DiscordEmbed embed = new DiscordEmbed("Player Detected!", "Player: " + player.getName().getString() + "\nCoordinates: " + player.getBlockPos().toShortString(), 0xFFFF00);
                        DiscordWebhook.sendMessage("", embed);
                        reportedPlayers.put(player, System.currentTimeMillis());
                    }
                }
            }
        }
    }
}
