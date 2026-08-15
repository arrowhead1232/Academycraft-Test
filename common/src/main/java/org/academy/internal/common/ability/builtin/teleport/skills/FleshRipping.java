package org.academy.internal.common.ability.builtin.teleport.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;
import org.academy.internal.common.ability.builtin.teleport.TeleporterSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

/** Rips an aimed living target through a short dimensional fracture. */
public final class FleshRipping extends SimpleInstantSkill {
    public static final FleshRipping INSTANCE = new FleshRipping();

    private FleshRipping() {
        super(
                SkillNames.FLESH_RIPPING, 3, 9500,
                List.of(MarkTeleport.INSTANCE, PenetrateTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.FLESH_RIPPING_ICON,
                130, 12,
                GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_ALT,
                190, 70, 0.005F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.getEyePosition();
        Vec3 end = SkillEffects.trace(player, 6.0 + 8.0 * proficiency);
        LivingEntity target = SkillEffects.livingAlongPath(player, start, end, 0.85)
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);
        if (target == null) return false;

        if (!TeleporterSkillEffects.attack(player, target, 5.0F + 7.0F * proficiency)) return false;
        if (player.getRandom().nextFloat() < 0.05F) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }
        SkillEffects.particles(
                player.serverLevel(), ParticleTypes.PORTAL,
                target.getBoundingBox().getCenter(), 36, 0.55, 0.08
        );
        return true;
    }
}
