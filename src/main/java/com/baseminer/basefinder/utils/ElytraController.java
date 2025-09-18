package com.baseminer.basefinder.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ElytraController {
    private static List<Vec3d> waypoints = new ArrayList<>();
    private static int currentWaypoint = 0;
    private static boolean active = false;

    public static void start(int x1, int z1, int x2, int z2, int stripWidth) {
        waypoints.clear();
        currentWaypoint = 0;

        // Generate waypoints
        boolean forward = true;
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += stripWidth) {
            if (forward) {
                waypoints.add(new Vec3d(x, Config.flightAltitude, z1));
                waypoints.add(new Vec3d(x, Config.flightAltitude, z2));
            } else {
                waypoints.add(new Vec3d(x, Config.flightAltitude, z2));
                waypoints.add(new Vec3d(x, Config.flightAltitude, z1));
            }
            forward = !forward;
        }


        active = true;
        flyToNextWaypoint();
    }

    public static void stop() {
        active = false;
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.networkHandler.sendChatCommand("elytrapilot off");
        }
    }

    public static void onTick() {
        if (!active || MeteorClient.mc.player == null) return;

        if (currentWaypoint >= waypoints.size()) {
            stop();
            return;
        }

        Vec3d target = waypoints.get(currentWaypoint);
        if (MeteorClient.mc.player.getPos().distanceTo(target) < 10) {
            flyToNextWaypoint();
        }
    }

    private static void flyToNextWaypoint() {
        if (currentWaypoint >= waypoints.size()) {
            stop();
            return;
        }

        Vec3d waypoint = waypoints.get(currentWaypoint);
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.networkHandler.sendChatCommand("elytrapilot destination " + (int)waypoint.x + " " + (int)waypoint.z);
        }
        currentWaypoint++;
    }

    public static boolean isActive() {
        return active;
    }
}
