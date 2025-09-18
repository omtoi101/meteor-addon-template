package com.baseminer.basefinder.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

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
}
