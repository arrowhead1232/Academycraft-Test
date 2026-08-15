package org.academy.internal.common.ability.builtin.meltdowner.skills;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.meltdowner.Meltdowner;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MineRayLuck extends MineRaySkill {
    public static final MineRayLuck INSTANCE = new MineRayLuck();

    private MineRayLuck() {
        super(
                SkillNames.MINE_RAY_LUCK, 5, 15000, List.of(MineRayExpert.INSTANCE),
                () -> Meltdowner.INSTANCE,
                TextureResources.MINE_RAY_LUCK_ICON,
                205, 82, GLFW.GLFW_KEY_U,
                35.0F, 60,
                20.0, 0.50F, 1.0F, 3, 0.0003F
        );
    }

    @Override
    protected ItemStack miningTool() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(Enchantments.BLOCK_FORTUNE, 3);
        return tool;
    }
}
