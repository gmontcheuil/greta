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
package vib.tools.editors.fml.timelines;

import vib.tools.editors.SpeechUtil;
import vib.tools.editors.TimeLine;
import vib.core.intentions.PseudoIntentionSpeech;
import java.awt.FontMetrics;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class PseudoIntentionSpeechTimeLine extends TimeLine<PseudoIntentionSpeech> {

    @Override
    protected TemporizableContainer<PseudoIntentionSpeech> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<PseudoIntentionSpeech>(
                            new PseudoIntentionSpeech(SpeechUtil.instanciateTemporizable(startTime, endTime)),
                            manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<PseudoIntentionSpeech> temporizableContainer, FontMetrics metrics, int limitSize) {
        return SpeechUtil.getDescription(temporizableContainer.getTemporizable(), metrics, limitSize);
    }

}
