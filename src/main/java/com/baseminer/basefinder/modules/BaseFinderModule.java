package com.baseminer.basefinder.modules;

import com.baseminer.basefinder.BaseFinder;
import com.baseminer.basefinder.utils.Config;
import com.baseminer.basefinder.utils.DiscordWebhook;
import com.baseminer.basefinder.utils.ElytraController;
import com.baseminer.basefinder.events.PlayerDeathEvent;
import com.baseminer.basefinder.utils.WorldScanner;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BaseFinderModule extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<String> discordWebhookUrl = sgGeneral.add(new StringSetting.Builder()
        .name("discord-webhook-url")
        .description("The Discord webhook URL to send notifications to.")
        .defaultValue(Config.discordWebhookUrl)
        .onChanged(v -> Config.discordWebhookUrl = v)
        .build()
    );

    private final Setting<Integer> blockDetectionThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("block-detection-threshold")
        .description("The number of valuable blocks to find before a base is detected.")
        .defaultValue(Config.blockDetectionThreshold)
        .onChanged(v -> Config.blockDetectionThreshold = v)
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("The radius to scan for valuable blocks.")
        .defaultValue(Config.scanRadius)
        .onChanged(v -> Config.scanRadius = v)
        .build()
    );

    private final Setting<Boolean> playerDetection = sgGeneral.add(new BoolSetting.Builder()
        .name("player-detection")
        .description("Whether to notify when a player is detected.")
        .defaultValue(Config.playerDetection)
        .onChanged(v -> Config.playerDetection = v)
        .build()
    );

    private final Setting<Boolean> notifyOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-on-death")
        .description("Whether to notify when you die.")
        .defaultValue(Config.notifyOnDeath)
        .onChanged(v -> Config.notifyOnDeath = v)
        .build()
    );


    public BaseFinderModule() {
        super(BaseFinder.CATEGORY, "base-finder", "Automatically finds bases by flying around and scanning for valuable blocks.");
    }

    @Override
    public void onActivate() {
        // Start the bot
    }

    @Override
    public void onDeactivate() {
        ElytraController.stop();
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        ElytraController.onChatMessage(event.getMessage().getString());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) {
            return;
        }


        List<BlockPos> valuableBlocks = WorldScanner.scanForValuableBlocks(mc.player.getPos(), scanRadius.get());

        if (valuableBlocks.size() >= blockDetectionThreshold.get()) {
            String coords = valuableBlocks.get(0).toShortString();
            DiscordWebhook.sendMessage("Base found at: " + coords);
            info("Base found at: " + coords);
            toggle(); // Disable the module after finding a base
        }
    }

    @EventHandler
    private void onTickPlayerDetection(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || !playerDetection.get()) {
            return;
        }


        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) {
                continue;
            }

            if (mc.player.distanceTo(player) < 100) {
                DiscordWebhook.sendMessage("Player detected: " + player.getName().getString() + " at " + player.getBlockPos().toShortString());
            }
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (notifyOnDeath.get() && event.player == mc.player) {
            DiscordWebhook.sendMessage("Bot died at " + event.player.getBlockPos().toShortString());
        }
    }
}
