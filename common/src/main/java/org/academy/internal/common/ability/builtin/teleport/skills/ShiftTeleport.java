package org.academy.internal.common.ability.builtin.teleport.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;
import org.academy.internal.common.ability.builtin.teleport.TeleporterSkillEffects;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Teleports one held block to the aimed surface and damages entities in its path. */
public final class ShiftTeleport extends SimpleInstantSkill {
    public static final ShiftTeleport INSTANCE = new ShiftTeleport();

    private ShiftTeleport() {
        super(
                SkillNames.SHIFT_TELEPORT, 4, 10000, List.of(LocationTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.SHIFT_TELEPORT_ICON,
                175, 47,
                GLFW.GLFW_KEY_J, GLFW.GLFW_MOD_ALT,
                220, 80, 0.004F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem)) return false;

        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(25.0 + 10.0 * proficiency));
        HitResult hit = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));
        if (!(hit instanceof BlockHitResult blockHit)) return false;

        BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        BlockState existing = level.getBlockState(targetPos);
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (!existing.canBeReplaced() || !state.canSurvive(level, targetPos)) return false;
        if (!level.setBlock(targetPos, state, Block.UPDATE_ALL)) return false;

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        Vec3 targetCenter = Vec3.atCenterOf(targetPos);
        for (var target : SkillEffects.livingAlongPath(player, start, targetCenter, 0.8)) {
            TeleporterSkillEffects.attack(player, target, 10.0F + 18.0F * proficiency);
        }
        SkillEffects.particles(level, ParticleTypes.PORTAL, targetCenter, 48, 0.7, 0.1);
        return true;
    }
}
