package org.academy.internal.common.ability.builtin.teleport.skills;

import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimplePassiveSkill;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;

import java.util.List;

/** Passive dimensional critical-hit chance used by Teleporter attacks. */
public final class DimFoldingTheorem extends SimplePassiveSkill {
    public static final DimFoldingTheorem INSTANCE = new DimFoldingTheorem();

    private DimFoldingTheorem() {
        super(
                SkillNames.DIM_FOLDING_THEOREM, 1, 5000, List.of(SelfTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.DIM_FOLDING_THEOREM_ICON,
                50, 75
        );
    }
}
