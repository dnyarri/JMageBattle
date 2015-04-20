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

package arkhados.spell.spells.rockgolem;

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;


public class Bedrock extends Spell{
    {   
        iconName = "MineralArmor.png";
    }

    public Bedrock(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 13f;
        final float range = 0f;
        final float castTime = 0f;

        final Bedrock spell = new Bedrock("Bedrock", cooldown,
                range, castTime);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                CastSelfBuffAction action = new CastSelfBuffAction();
                ArmorBuff armor = new ArmorBuff(500f, 0.55f, -1, 4f);
                armor.setTypeId(BuffTypeIds.BEDROCK);
                armor.setOwnerInterface(caster
                        .getControl(CInfluenceInterface.class));
                action.addBuff(armor);
                SlowCC slow = new SlowCC(-1, 4f, 0.75f);
                slow.setTypeId(-1);
                action.addBuff(slow);
                return action;
            }
        };

        spell.nodeBuilder = null;

        return spell;
    }

}
