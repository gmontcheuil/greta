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
package vib.core.signals;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 * This interface descibes an object that can receive and use a list of
 * {@code Signal}.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @inavassoc - "used by" * vib.core.signals.Signal
 */
public interface SignalPerformer {

    /**
     * This fuction receives a list of {@code Signal}.<br/> This function is
     * typically call by {@code SignalEmitters}.
     *
     * @param signals the list of {@code Signal}
     * @param requestId the identifier of the request
     * @param mode how the list is added to previous list : blend, replace,
     * append
     */
    public void performSignals(List<Signal> signals, ID requestId, Mode mode);
}
