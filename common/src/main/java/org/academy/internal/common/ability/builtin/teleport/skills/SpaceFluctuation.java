package org.academy.internal.common.ability.builtin.teleport.skills;

import org.academy.api.client.resource.TextureResources;
import org.academy.internal.common.ability.builtin.SimplePassiveSkill;
import org.academy.internal.common.ability.builtin.SkillNames;
import org.academy.internal.common.ability.builtin.teleport.Teleport;

import java.util.List;

/** Advanced passive adding higher tiers of dimensional critical hits. */
public final class SpaceFluctuation extends SimplePassiveSkill {
    public static final SpaceFluctuation INSTANCE = new SpaceFluctuation();

    private SpaceFluctuation() {
        super(
                SkillNames.SPACE_FLUCTUATION, 4, 11500, List.of(ShiftTeleport.INSTANCE),
                () -> Teleport.INSTANCE,
                TextureResources.SPACE_FLUCTUATION_ICON,
                160, 80
        );
    }
}
