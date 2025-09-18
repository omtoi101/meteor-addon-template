package com.baseminer.basefinder.mixin;

import com.baseminer.basefinder.events.PlayerDeathEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(PlayerDeathEvent.get((PlayerEntity) (Object) this));
    }
}
