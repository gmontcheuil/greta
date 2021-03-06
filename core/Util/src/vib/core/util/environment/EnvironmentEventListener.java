/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.environment;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */

public interface EnvironmentEventListener {

    // 3 kinds of events:
    // events on tree changes
    // events on node changes
    // *events on animations*

    /**
     *
     * @param e
     */
    public void onTreeChange(TreeEvent e);

    /**
     *
     * @param e
     */
    public void onNodeChange(NodeEvent e);

    public void onLeafChange(LeafEvent event);

}
