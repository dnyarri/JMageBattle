/*    This file is part of <project>.

 <project> is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 <project> is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with <project>.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.spell;

import arkhados.actions.EntityAction;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public interface CastSpellActionBuilder {
    public EntityAction newAction(Node caster, Vector3f vec);
}