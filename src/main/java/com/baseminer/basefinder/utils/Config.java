package com.baseminer.basefinder.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class Config {
    private static final File CONFIG_FILE = new File(MeteorClient.FOLDER, "base-finder.properties");
    private static final Properties properties = new Properties();

    public static String discordWebhookUrl = "";
    public static int blockDetectionThreshold = 10;
    public static int scanRadius = 32;
    public static List<Block> valuableBlocks = new ArrayList<>(Arrays.asList(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX
    ));
    public static boolean playerDetection = true;
    public static boolean notifyOnDeath = true;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                properties.load(fis);
                discordWebhookUrl = properties.getProperty("discordWebhookUrl", "");
                blockDetectionThreshold = Integer.parseInt(properties.getProperty("blockDetectionThreshold", "10"));
                scanRadius = Integer.parseInt(properties.getProperty("scanRadius", "32"));
                playerDetection = Boolean.parseBoolean(properties.getProperty("playerDetection", "true"));
                notifyOnDeath = Boolean.parseBoolean(properties.getProperty("notifyOnDeath", "true"));
                // valuableBlocks are not configurable for now, to keep it simple
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.setProperty("discordWebhookUrl", discordWebhookUrl);
            properties.setProperty("blockDetectionThreshold", String.valueOf(blockDetectionThreshold));
            properties.setProperty("scanRadius", String.valueOf(scanRadius));
            properties.setProperty("playerDetection", String.valueOf(playerDetection));
            properties.setProperty("notifyOnDeath", String.valueOf(notifyOnDeath));
            properties.store(fos, "Base Finder Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
