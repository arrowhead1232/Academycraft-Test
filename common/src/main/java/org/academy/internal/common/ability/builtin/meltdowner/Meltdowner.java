package org.academy.internal.common.ability.builtin.meltdowner;

import org.academy.api.common.ability.AbilityCategory;
import org.academy.internal.common.ability.builtin.AbilityCategoryNames;
import org.academy.internal.common.ability.builtin.meltdowner.skills.*;

public class Meltdowner extends AbilityCategory {
    public static final AbilityCategory INSTANCE = new Meltdowner();

    private Meltdowner() {
        super(AbilityCategoryNames.MELTDOWNER);
        this.skillList.add(ElectronBomb.INSTANCE);
        this.skillList.add(RadiationIntensify.INSTANCE);
        this.skillList.add(ScatterBomb.INSTANCE);
        this.skillList.add(LightShield.INSTANCE);
        this.skillList.add(SingleHighSpeedElectronBeam.INSTANCE);
        this.skillList.add(MineRayBasic.INSTANCE);
        this.skillList.add(RayBarrage.INSTANCE);
        this.skillList.add(JetEngine.INSTANCE);
        this.skillList.add(MineRayExpert.INSTANCE);
        this.skillList.add(MineRayLuck.INSTANCE);
        this.skillList.add(ElectronMissile.INSTANCE);
    }
}
