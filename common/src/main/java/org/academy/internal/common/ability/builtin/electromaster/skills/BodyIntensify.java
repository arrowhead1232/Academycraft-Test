package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** A compact port of 1.1.3's charged physical-enhancement buff. */
public final class BodyIntensify extends SimpleInstantSkill {
    public static final BodyIntensify INSTANCE = new BodyIntensify();

    private BodyIntensify() {
        super(
                SkillNames.BODY_INTENSIFY, 3, 7500,
                List.of(ArcGenerate.INSTANCE, CurrentCharging.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.BODY_INTENSIFY_ICON,
                110, 38,
                GLFW.GLFW_KEY_I, GLFW.GLFW_MOD_ALT,
                90, 600, 0.01F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        int duration = 200 + Math.round(200 * proficiency);
        int amplifier = proficiency >= 0.65F ? 1 : 0;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1 + amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, Math.max(80, duration / 3), 1));
        return true;
    }
}
