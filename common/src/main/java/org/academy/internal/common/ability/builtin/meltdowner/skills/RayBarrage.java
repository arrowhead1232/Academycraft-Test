package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.academy.internal.common.ability.builtin.meltdowner.MeltdownerSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Wide electron-ray barrage with a stronger central beam. */
public final class RayBarrage extends SimpleInstantSkill {
    public static final RayBarrage INSTANCE = new RayBarrage();

    private RayBarrage() {
        super(
                SkillNames.RAY_BARRAGE, 4, 12500, List.of(SingleHighSpeedElectronBeam.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.RAY_BARRAGE_ICON,
                140, 10,
                GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_ALT,
                420, 100, 0.005F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        Vec3 start = player.getEyePosition();
        Vec3 end = SkillEffects.trace(player, 20.0);
        Set<LivingEntity> central = new HashSet<>(
                SkillEffects.livingAlongPath(player, start, end, 0.75)
        );
        List<LivingEntity> targets = SkillEffects.livingAlongPath(player, start, end, 4.0);
        float centralDamage = 25.0F + 35.0F * proficiency;
        float scatteredDamage = 10.0F + 8.0F * proficiency;
        for (LivingEntity target : targets) {
            MeltdownerSkillEffects.attack(
                    player, target, central.contains(target) ? centralDamage : scatteredDamage
            );
        }
        SkillEffects.particles(player.serverLevel(), ParticleTypes.END_ROD, end, 100, 2.2, 0.15);
        SkillEffects.particles(player.serverLevel(), ParticleTypes.ELECTRIC_SPARK, end, 44, 1.4, 0.10);
        return true;
    }
}
