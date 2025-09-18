package com.baseminer.basefinder.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldScanner {
    public static List<BlockPos> scanForValuableBlocks(Vec3d center, int radius) {
        List<BlockPos> valuableBlocks = new ArrayList<>();
        if (MinecraftClient.getInstance().world == null) {
            return valuableBlocks;
        }

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = new BlockPos((int)center.x + x, (int)center.y + y, (int)center.z + z);
                    if (Config.valuableBlocks.contains(MinecraftClient.getInstance().world.getBlockState(pos).getBlock())) {
                        valuableBlocks.add(pos);
                    }
                }
            }
        }

        return valuableBlocks;
    }

    public static double getBoundingBoxVolume(List<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            return 0;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos block : blocks) {
            minX = Math.min(minX, block.getX());
            minY = Math.min(minY, block.getY());
            minZ = Math.min(minZ, block.getZ());
            maxX = Math.max(maxX, block.getX());
            maxY = Math.max(maxY, block.getY());
            maxZ = Math.max(maxZ, block.getZ());
        }


        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public static Map<Block, Integer> countBlocks(List<BlockPos> blocks) {
        Map<Block, Integer> counts = new HashMap<>();
        if (MinecraftClient.getInstance().world == null) {
            return counts;
        }

        for (BlockPos blockPos : blocks) {
            Block block = MinecraftClient.getInstance().world.getBlockState(blockPos).getBlock();
            counts.put(block, counts.getOrDefault(block, 0) + 1);
        }

        return counts;
    }
}
