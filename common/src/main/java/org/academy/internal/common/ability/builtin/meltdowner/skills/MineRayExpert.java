package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MineRayExpert extends MineRaySkill {
    public static final MineRayExpert INSTANCE = new MineRayExpert();

    private MineRayExpert() {
        super(
                SkillNames.MINE_RAY_EXPERT, 4, 12500, List.of(MineRayBasic.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.MINE_RAY_EXPERT_ICON,
                172, 70, GLFW.GLFW_KEY_N,
                18.0F, 60,
                20.0, 0.50F, 1.0F, 3, 0.0003F
        );
    }

    @Override
    protected ItemStack miningTool() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }
}
