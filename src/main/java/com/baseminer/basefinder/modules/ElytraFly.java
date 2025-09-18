package com.baseminer.basefinder.modules;

import com.baseminer.basefinder.BaseFinder;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
    public Vec3d target;

    public ElytraFly() {
        super(BaseFinder.CATEGORY, "elytra-fly-simple", "A simple elytra flight module for BaseFinder.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) {
            toggle();
            return;
        }
        if (!mc.player.getAbilities().flying && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            mc.player.jump();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || !mc.player.getAbilities().flying) {
            return;
        }

        if (target != null) {
            double
                px = mc.player.getX(),
                py = mc.player.getY(),
                pz = mc.player.getZ();

            double
                dx = target.x - px,
                dy = target.y - py,
                dz = target.z - pz;

            double yaw = Math.atan2(dz, dx);
            double pitch = Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);

            mc.player.setYaw((float) Math.toDegrees(yaw) - 90);
            mc.player.setPitch((float) Math.toDegrees(pitch));
        }

        Vec3d forward = new Vec3d(0, 0, 0.1).rotateY(-(float) Math.toRadians(mc.player.getYaw()));
        mc.player.setVelocity(forward);
    }
}
