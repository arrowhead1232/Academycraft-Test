package org.academy.internal.common.ability.builtin.electromaster.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimpleInstantSkill;
import org.academy.internal.common.ability.builtin.SkillEffects;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.electromaster.Electromaster;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Server-side ore scan with a short particle reveal. */
public final class MineDetect extends SimpleInstantSkill {
    public static final MineDetect INSTANCE = new MineDetect();

    private MineDetect() {
        super(
                SkillNames.MINE_DETECT, 3, 8500, List.of(MagnetManipulation.INSTANCE),
                () -> Electromaster.INSTANCE,
                TextureResources.MINE_DETECT_ICON,
                225, 12,
                GLFW.GLFW_KEY_O, GLFW.GLFW_MOD_ALT,
                180, 400, 0.008F
        );
    }

    @Override
    protected boolean perform(ServerPlayer player, float proficiency) {
        int radius = 12 + Math.round(8 * proficiency);
        BlockPos center = player.blockPosition();
        int revealed = 0;
        for (BlockPos position : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (!isOre(player.serverLevel().getBlockState(position))) continue;
            SkillEffects.particles(
                    player.serverLevel(), ParticleTypes.ENCHANT,
                    Vec3.atCenterOf(position), 3, 0.35, 0.02
            );
            if (++revealed >= 128) break;
        }
        return true;
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES);
    }
}
