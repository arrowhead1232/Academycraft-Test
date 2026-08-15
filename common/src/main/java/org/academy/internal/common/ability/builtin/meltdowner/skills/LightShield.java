package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Timed defensive approximation of the original sustained electron shield. */
public final class LightShield extends SimpleInstantSkill {
    public static final LightShield INSTANCE = new LightShield();

    private LightShield() {
        super(
                SkillNames.LIGHT_SHIELD, 2, 7500, List.of(SingleHighSpeedElectronBeam.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.LIGHT_SHIELD_ICON,
                55, 15,
                GLFW.GLFW_KEY_L, GLFW.GLFW_MOD_ALT,
                90, 180, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        int duration = 120 + Math.round(80 * proficiency);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 1 + Math.round(2 * proficiency)));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, proficiency > 0.7F ? 2 : 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
        SkillEffects.particles(
                player.serverLevel(), ParticleTypes.END_ROD,
                player.getBoundingBox().getCenter(), 48, 1.0, 0.04
        );
        return true;
    }
}
