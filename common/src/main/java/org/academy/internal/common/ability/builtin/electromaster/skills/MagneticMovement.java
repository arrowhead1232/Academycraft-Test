package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleSustainedSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Pulls the player toward an aimed metallic block while the key is held. */
public final class MagneticMovement extends SimpleSustainedSkill {
    public static final MagneticMovement INSTANCE = new MagneticMovement();

    private MagneticMovement() {
        super(
                SkillNames.MAGNETIC_MOVEMENT, 2, 6500,
                List.of(ArcGenerate.INSTANCE, CurrentCharging.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.MAGNETIC_MOVEMENT_ICON,
                137, 35,
                GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_ALT,
                1.5F, 30, 0.0002F
        );
    }

    @Override
    protected Object begin(ServerPlayer player, float proficiency) {
        HitResult hit = player.pick(25.0, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return null;
        BlockPos position = blockHit.getBlockPos();
        return isMetalTarget(player.serverLevel().getBlockState(position))
                ? position.immutable() : null;
    }

    @Override
    protected boolean tick(ServerPlayer player, float proficiency, Object state, int ticks) {
        BlockPos position = (BlockPos) state;
        if (!isMetalTarget(player.serverLevel().getBlockState(position))) return false;
        Vec3 target = Vec3.atCenterOf(position);
        Vec3 delta = target.subtract(player.getEyePosition());
        if (delta.lengthSqr() < 2.25 || delta.lengthSqr() > 900.0) return false;

        double acceleration = 0.10 + 0.08 * proficiency;
        Vec3 movement = player.getDeltaMovement().scale(0.72)
                .add(delta.normalize().scale(acceleration));
        if (movement.lengthSqr() > 2.25) movement = movement.normalize().scale(1.5);
        player.setDeltaMovement(movement);
        player.resetFallDistance();
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        if (ticks % 4 == 0) {
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.ELECTRIC_SPARK,
                    player.getBoundingBox().getCenter(), 5, 0.3, 0.03
            );
        }
        return true;
    }

    private static boolean isMetalTarget(BlockState state) {
        return state.is(BlockTags.RAILS)
                || state.is(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.COPPER_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.RAW_IRON_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.RAW_GOLD_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.RAW_COPPER_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.IRON_BARS)
                || state.is(net.minecraft.world.level.block.Blocks.HOPPER)
                || state.is(net.minecraft.world.level.block.Blocks.ANVIL)
                || state.is(net.minecraft.world.level.block.Blocks.CHIPPED_ANVIL)
                || state.is(net.minecraft.world.level.block.Blocks.DAMAGED_ANVIL)
                || state.is(net.minecraft.world.level.block.Blocks.CHAIN)
                || state.is(net.minecraft.world.level.block.Blocks.CAULDRON)
                || state.is(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD)
                || state.is(net.minecraft.world.level.block.Blocks.IRON_DOOR)
                || state.is(net.minecraft.world.level.block.Blocks.IRON_TRAPDOOR)
                || state.is(net.minecraft.world.level.block.Blocks.PISTON)
                || state.is(net.minecraft.world.level.block.Blocks.STICKY_PISTON);
    }
}
