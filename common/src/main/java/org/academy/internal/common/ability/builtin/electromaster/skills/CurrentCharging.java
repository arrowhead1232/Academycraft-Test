package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.api.common.wireless.WirelessUser;
import org.academy.internal.common.ability.builtin.SimpleSustainedSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Converts computing power into Academy energy for an aimed machine. */
public final class CurrentCharging extends SimpleSustainedSkill {
    public static final CurrentCharging INSTANCE = new CurrentCharging();

    private CurrentCharging() {
        super(
                SkillNames.CURRENT_CHARGING, 1, 5000, List.of(ArcGenerate.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.CURRENT_CHARGING_ICON,
                55, 18,
                GLFW.GLFW_KEY_Q, GLFW.GLFW_MOD_ALT,
                2.5F, 40, 0.0001F
        );
    }

    @Override
    protected Object begin(ServerPlayer player, float proficiency) {
        HitResult hit = player.pick(15.0, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return null;
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(blockHit.getBlockPos());
        return blockEntity instanceof WirelessUser ? blockHit.getBlockPos().immutable() : null;
    }

    @Override
    protected boolean tick(ServerPlayer player, float proficiency, Object state, int ticks) {
        BlockPos position = (BlockPos) state;
        if (player.distanceToSqr(Vec3.atCenterOf(position)) > 256.0) return false;
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(position);
        if (!(blockEntity instanceof WirelessUser user)) return false;

        int amount = Math.round(15.0F + 20.0F * proficiency);
        int accepted = user.receiveEnergy(amount, false);
        if (ticks % 4 == 0) {
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.ELECTRIC_SPARK,
                    Vec3.atCenterOf(position), 8, 0.35, 0.04
            );
        }
        return accepted > 0;
    }
}
