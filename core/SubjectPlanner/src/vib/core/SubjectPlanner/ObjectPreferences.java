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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.SubjectPlanner;

import java.util.ArrayList;
import java.util.Collections;
import vib.core.util.parameter.EngineParameter;
import vib.core.util.parameter.EngineParameterSet;

/**
 *
 * @author Nadine
 */
public class ObjectPreferences {

    private final AgentPreferenceObject agentPreferenceObject;
    private final UserPreferenceObject userPreferenceObject;

    public ObjectPreferences() {

        agentPreferenceObject = new AgentPreferenceObject(AgentPreferenceObject.AGENT_OBJ_PREF);
        userPreferenceObject = new UserPreferenceObject(UserPreferenceObject.OBJECT_PREF_USER_LIB);
    }

    public double GetObjectPreferenceAgent(String ObjectName) {
        double ObjectPreferenceAgent = 0;
        EngineParameterSet prefs = agentPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()) {
            if (pref.getParamName().equalsIgnoreCase(ObjectName)) {
                ObjectPreferenceAgent = pref.getValue();
            }
        }
        return ObjectPreferenceAgent;
    }
    
    public double GetMaxObjectPreferenceAgent(){
        double max = 0;
        ArrayList<Double> prefValues = new ArrayList();
        EngineParameterSet prefs = agentPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()){
            prefValues.add(Math.abs(pref.getValue()));
        }
        return Collections.max(prefValues);
    }
    
        public double GetObjectPreferenceUser(String ObjectName) {
        double ObjectPreferenceUser = 0;
        EngineParameterSet prefs = userPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()) {
            if (pref.getParamName().equalsIgnoreCase(ObjectName)) {
                ObjectPreferenceUser = pref.getValue();
            }
        }
        return ObjectPreferenceUser;
    }
    
}
