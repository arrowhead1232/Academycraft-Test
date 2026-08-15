package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MineRayBasic extends MineRaySkill {
    public static final MineRayBasic INSTANCE = new MineRayBasic();

    private MineRayBasic() {
        super(
                SkillNames.MINE_RAY_BASIC, 3, 10000, List.of(SingleHighSpeedElectronBeam.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.MINE_RAY_BASIC_ICON,
                140, 70, GLFW.GLFW_KEY_B,
                10.0F, 40,
                10.0, 0.20F, 0.40F, 2, 0.0005F
        );
    }

    @Override
    protected ItemStack miningTool() {
        return new ItemStack(Items.IRON_PICKAXE);
    }
}
