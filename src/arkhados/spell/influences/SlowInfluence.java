/*    This file is part of Arkhados.

 Arkhados is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arkhados is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arkhados.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.spell.influences;

import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SlowInfluence extends AbstractInfluence {

    private float slowFactor = 1;

    @Override
    public boolean isFriendly() {
        return false;
    }

    @Override
    public void affect(InfluenceInterfaceControl targetInterface, float tpf) {
        if (targetInterface.isSpeedConstant()) {
            return;
        }

        Spatial spatial = targetInterface.getSpatial();

        float msCurrent = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        msCurrent *= slowFactor;
        spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msCurrent);
    }

    public float getSlowFactor() {
        return slowFactor;
    }

    public void setSlowFactor(float slowFactor) {
        this.slowFactor = slowFactor;
    }
}