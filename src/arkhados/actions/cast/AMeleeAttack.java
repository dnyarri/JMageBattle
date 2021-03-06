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
package arkhados.actions.cast;

import arkhados.CharacterInteraction;
import arkhados.SpatialDistancePair;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AMeleeAttack extends EntityAction {

    private final List<AbstractBuffBuilder> buffs = new ArrayList<>();
    private final float damage;
    private final float range;

    public AMeleeAttack(float damage, float range) {
        this.damage = damage;
        this.range = range;
    }

    public void addBuff(AbstractBuffBuilder buff) {
        buffs.add(buff);
    }

    public void setTypeIdOnHit() {
        // override this in inherited classes
    }

    @Override
    public boolean update(float tpf) {
        CCharacterPhysics physicsControl = spatial
                .getControl(CCharacterPhysics.class);
        Vector3f hitDirection = physicsControl.calculateTargetDirection()
                .normalize().multLocal(range);

        final int myTeamId = spatial.getUserData(UserData.TEAM_ID);

        physicsControl.setViewDirection(hitDirection);

        Predicate<SpatialDistancePair> pred = (SpatialDistancePair value) -> {
            if (value.spatial == spatial) {
                return false;
            }

            Integer nullableTeamId
                    = value.spatial.getUserData(UserData.TEAM_ID);
            if (nullableTeamId == null) {
                return false;
            }

            CInfluenceInterface influenceInterface = value.spatial
                    .getControl(CInfluenceInterface.class);

            return influenceInterface != null
                    && !nullableTeamId.equals(myTeamId);
        };

        SpatialDistancePair closest = Selector.giveClosest(
                Selector.coneSelect(new ArrayList<SpatialDistancePair>(), pred,
                        spatial.getLocalTranslation(), hitDirection,
                        range, (float) Math.toRadians(50f)));

        if (closest == null) {
            return false;
        }

        CInfluenceInterface targetInterface
                = closest.spatial.getControl(CInfluenceInterface.class);
        if (targetInterface != null) {
            CActionQueue cQueue = targetInterface.getSpatial()
                    .getControl(CActionQueue.class);
            EntityAction aCurrent = cQueue.getCurrent();

            if (aCurrent instanceof ATrance) {
                ((ATrance) aCurrent).activate(spatial);
                return false;
            }

            float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);
            float rawDamage = damage * damageFactor;
            // TODO: Calculate damage for possible Damage over Time -buffs
            CharacterInteraction.harm(
                    spatial.getControl(CInfluenceInterface.class),
                    targetInterface, rawDamage, buffs, true);
            setTypeIdOnHit();
        }

        return false;
    }
}
