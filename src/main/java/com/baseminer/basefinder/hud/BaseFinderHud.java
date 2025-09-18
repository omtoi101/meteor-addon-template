package com.baseminer.basefinder.hud;

import com.baseminer.basefinder.BaseFinder;
import com.baseminer.basefinder.utils.ElytraController;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class BaseFinderHud extends HudElement {
    public static final HudElementInfo<BaseFinderHud> INFO = new HudElementInfo<>(BaseFinder.HUD_GROUP, "base-finder-status", "Displays the status of the Base Finder bot.", BaseFinderHud::new);

    public BaseFinderHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        String status = "Idle";
        if (ElytraController.isActive()) {
            status = "Flying to waypoint";
        }

        setSize(renderer.textWidth(status, true), renderer.textHeight(true));
        renderer.text(status, x, y, Color.WHITE, true);
    }
}
