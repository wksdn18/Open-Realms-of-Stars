package org.openRealmOfStars.player.diplomacy;

import org.openRealmOfStars.player.SpaceRace.SpaceRace;

/**
 *
 * Open Realm of Stars game project Copyright (C) 2017 Tuomo Untinen
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see http://www.gnu.org/licenses/
 *
 *
 * SameRaceBonus
 */
public class SameRaceBonus implements Bonus {

    /**
     * calculate BonusValue for SameRace
     * @param race
     * @return BonusValue
     */
    @Override
    public int BonusValue(SpaceRace race) {
        if (race == SpaceRace.SPORKS)
            return 2;
        else if (race == SpaceRace.MECHIONS)
            return -3;
        else
            return -5;
    }

    

    /**
     * calculate BonusLasting for SameRace
     * @param race
     * @return BonusLasting
     */
    @Override
    public int BonusLasting(SpaceRace race) {
        if (race == SpaceRace.SPORKS)
            return 255;
        else if (race == SpaceRace.MECHIONS)
            return 255;
        else
            return 255;
    }
}