package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Pulls metallic mobs, minecarts and dropped items toward the user. */
public final class MagnetManipulation extends SimpleInstantSkill {
    public static final MagnetManipulation INSTANCE = new MagnetManipulation();

    private MagnetManipulation() {
        super(
                SkillNames.MAGNET_MANIPULATION, 3, 5000, List.of(MagneticMovement.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.MAGNET_MANIPULATION_ICON,
                75, 70,
                GLFW.GLFW_KEY_M, GLFW.GLFW_MOD_ALT,
                30, 20, 0.003F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        double range = 10.0 + 6.0 * proficiency;
        AABB area = player.getBoundingBox().inflate(range);
        List<Entity> targets = player.serverLevel().getEntities(
                player,
                area,
                entity -> entity instanceof ItemEntity
                        || entity instanceof AbstractMinecart
                        || entity instanceof IronGolem
        );
        if (targets.isEmpty()) return false;

        Vec3 center = player.getEyePosition();
        for (Entity target : targets) {
            Vec3 pull = center.subtract(target.position());
            if (pull.lengthSqr() < 0.01) continue;
            double strength = 0.25 + 0.35 * proficiency;
            target.setDeltaMovement(target.getDeltaMovement().scale(0.4).add(pull.normalize().scale(strength)));
            target.hurtMarked = true;
        }
        SkillEffects.particles(
                player.serverLevel(), ParticleTypes.ELECTRIC_SPARK,
                center, 24, 1.2, 0.08
        );
        return true;
    }
}
