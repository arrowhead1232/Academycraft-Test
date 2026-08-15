package org.academy.internal.common.ability.builtin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.common.util.LevelUtil;

import java.util.List;

public final class SkillEffects {
    private SkillEffects() {
    }

    public static Vec3 trace(ServerPlayer player, double range) {
        return trace(player, player.getLookAngle(), range);
    }

    public static Vec3 trace(ServerPlayer player, Vec3 direction, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(direction.normalize().scale(range));
        HitResult hit = player.serverLevel().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    public static List<LivingEntity> livingAlongPath(
            ServerPlayer player, Vec3 start, Vec3 end, double radius
    ) {
        AABB area = new AABB(start, end).inflate(radius);
        return player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != player
                        && entity.isAlive()
                        && LevelUtil.distanceSqToLineSegment(
                        entity.getBoundingBox().getCenter(), start, end
                ) <= Math.pow(radius + entity.getBbWidth() * 0.5, 2)
        );
    }

    public static int damageAlongPath(
            ServerPlayer player, Vec3 start, Vec3 end, double radius, float damage
    ) {
        int hits = 0;
        for (LivingEntity target : livingAlongPath(player, start, end, radius)) {
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                hits++;
            }
        }
        return hits;
    }

    public static Vec3 teleportForward(ServerPlayer player, double distance) {
        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle().normalize();
        AABB originalBox = player.getBoundingBox();

        for (double current = distance; current >= 0.5; current -= 0.5) {
            Vec3 candidate = start.add(direction.scale(current));
            AABB candidateBox = originalBox.move(candidate.subtract(start));
            if (player.serverLevel().noCollision(player, candidateBox)) {
                if (player.isPassenger()) {
                    player.stopRiding();
                }
                player.teleportTo(candidate.x, candidate.y, candidate.z);
                player.resetFallDistance();
                player.setDeltaMovement(Vec3.ZERO);
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
                return candidate;
            }
        }
        return null;
    }

    public static void particles(
            ServerLevel level, ParticleOptions particle, Vec3 position,
            int count, double spread, double speed
    ) {
        level.sendParticles(
                particle,
                position.x, position.y, position.z,
                count,
                spread, spread, spread,
                speed
        );
    }
}
