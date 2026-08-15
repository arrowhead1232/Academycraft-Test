package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.common.ability.AbilityCategory;
import org.academy.api.common.ability.Skill;
import org.academy.internal.common.ability.builtin.SimpleSustainedSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillUse;

import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.function.Supplier;

/** Shared server-side beam mining logic for the three legacy Mine Ray tiers. */
abstract class MineRaySkill extends SimpleSustainedSkill {
    private final double range;
    private final float speedStart;
    private final float speedEnd;
    private final int miningTier;
    private final float proficiencyPerBlock;

    protected MineRaySkill(
            String name,
            int level,
            int energy,
            List<Skill> dependencies,
            Supplier<AbilityCategory> category,
            ResourceLocation icon,
            float treeX,
            float treeY,
            int key,
            float cpPerTick,
            int cooldownTicks,
            double range,
            float speedStart,
            float speedEnd,
            int miningTier,
            float proficiencyPerBlock
    ) {
        super(
                name, level, energy, dependencies, category, icon, treeX, treeY,
                key, org.lwjgl.glfw.GLFW.GLFW_MOD_ALT,
                cpPerTick, cooldownTicks, 0.0F
        );
        this.range = range;
        this.speedStart = speedStart;
        this.speedEnd = speedEnd;
        this.miningTier = miningTier;
        this.proficiencyPerBlock = proficiencyPerBlock;
    }

    @Override
    protected final Object begin(ServerPlayer player, float proficiency) {
        return new MiningState();
    }

    @Override
    protected final boolean tick(ServerPlayer player, float proficiency, Object rawState, int ticks) {
        MiningState mining = (MiningState) rawState;
        HitResult hit = player.pick(range, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            resetProgress(player.serverLevel(), player, mining);
            return true;
        }

        BlockPos position = blockHit.getBlockPos();
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(position);
        if (!canMine(player, level, position, state)) {
            resetProgress(level, player, mining);
            return true;
        }

        if (!position.equals(mining.position)) {
            resetProgress(level, player, mining);
            mining.position = position.immutable();
        }

        float hardness = Math.max(0.05F, state.getDestroySpeed(level, position));
        float speed = speedStart + (speedEnd - speedStart) * proficiency;
        mining.progress += speed;
        int stage = Math.min(9, Math.max(0, (int) (mining.progress / hardness * 10.0F)));
        level.destroyBlockProgress(player.getId(), position, stage);

        if (ticks % 3 == 0) {
            SkillEffects.particles(
                    level, ParticleTypes.END_ROD,
                    Vec3.atCenterOf(position), 4, 0.42, 0.025
            );
        }

        if (mining.progress >= hardness) {
            level.destroyBlockProgress(player.getId(), position, -1);
            if (breakBlock(level, player, position, state)) {
                SkillUse.grantProficiency(player, this, proficiencyPerBlock);
            }
            mining.position = null;
            mining.progress = 0.0F;
        }
        return true;
    }

    @Override
    protected final void end(ServerPlayer player, Object rawState, int ticks) {
        resetProgress(player.serverLevel(), player, (MiningState) rawState);
    }

    private boolean canMine(ServerPlayer player, ServerLevel level, BlockPos position, BlockState state) {
        if (!player.getAbilities().mayBuild || !level.mayInteract(player, position)) return false;
        if (state.isAir() || state.getDestroySpeed(level, position) < 0.0F) return false;
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL) && miningTier < 3) return false;
        if (state.is(BlockTags.NEEDS_IRON_TOOL) && miningTier < 2) return false;
        return !state.is(BlockTags.NEEDS_STONE_TOOL) || miningTier >= 1;
    }

    private boolean breakBlock(
            ServerLevel level, ServerPlayer player, BlockPos position, BlockState state
    ) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) return false;
        Block.dropResources(state, level, position, blockEntity, player, miningTool());
        level.levelEvent(2001, position, Block.getId(state));
        return true;
    }

    private static void resetProgress(ServerLevel level, ServerPlayer player, MiningState state) {
        if (state.position != null) level.destroyBlockProgress(player.getId(), state.position, -1);
        state.position = null;
        state.progress = 0.0F;
    }

    protected abstract ItemStack miningTool();

    private static final class MiningState {
        private BlockPos position;
        private float progress;
    }
}
