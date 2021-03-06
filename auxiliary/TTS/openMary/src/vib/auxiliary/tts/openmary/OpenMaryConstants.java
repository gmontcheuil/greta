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
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */

package vib.auxiliary.tts.openmary;

import java.util.HashMap;
import java.util.Map;
import vib.core.util.CharacterManager;
import vib.core.util.log.Logs;
import vib.core.util.speech.Boundary;
import vib.core.util.speech.Phoneme.PhonemeType;
import vib.core.util.speech.PitchAccent;
import vib.core.util.speech.Speech;
import vib.core.util.time.SynchPoint;
import vib.core.util.time.TimeMarker;

/**
 * Contains useful constants and functions with <a href="http://mary.dfki.de">Open Mary</a>.
 * @author Andre-Marie Pez
 */
public class OpenMaryConstants {

    /** Mary's parameter : used when sending a simple text */
    public static final String IN_TYPE_TEXT = "TEXT";

    /** Mary's parameter : used when sending the text in MaryXML format */
    public static final String IN_TYPE_MARYXML = "RAWMARYXML";

    /** Mary's parameter : used to obtain an audio stream */
    public static final String OUT_TYPE_AUDIO = "AUDIO";

    /** Mary's parameter : used to obtain an XML tree containing phonemes and timing informations */
    public static final String OUT_TYPE_PARAMS =
            "REALISED_ACOUSTPARAMS";
            //"ACOUSTPARAMS";

    /** Mary's parameter : used to obtain the audio stream in Wave format */
    public static final String AUDIO_TYPE_WAVE = "WAVE";

    /** Mary's parameter : used to obtain the audio stream in MP3 format */
    public static final String AUDIO_TYPE_MP3 = "MP3";

    /** values used with Open Mary 3.x.x */
    public static final int MARY_3 = 3;

    /** values used with Open Mary 4.x.x */
    public static final int MARY_4 = 4;

    /** Correspondence array of boundaries between VIB and Mary */
    public static final String[] BOUNDARY = {"L-", "H-", "L-%", "L-H%", "H-%", "H-^H%"};

    /** Correspondence array of pitch accents between VIB and Mary */
    public static final String[] PITCHACCENT = {"L*", "L*+H", "L+H*", "H*+L", "H+L*", "H*"};

    /** Correspondence map of phoneme between VIB and Mary */
    private static final Map<String,PhonemeType[]> correspondingPhonemes = new HashMap<String,PhonemeType[]>();

    /**
     * Static constructor.<br/>
     * Fills the phoneme corresponding map.
     */
    static{
        pho("E", PhonemeType.E1);
        pho("A", PhonemeType.O1);
        pho("EI", PhonemeType.e, PhonemeType.i);
        pho("V", PhonemeType.a1);
        pho("@U", PhonemeType.o1);
        pho("U", PhonemeType.u1);
        pho("O", PhonemeType.a, PhonemeType.o);
        pho("r=", PhonemeType.e);
        pho("j", PhonemeType.y);
        pho("u", PhonemeType.u);
        pho("T", PhonemeType.th);
        pho("{", PhonemeType.e1);
        pho("i", PhonemeType.i1);
        pho("D", PhonemeType.d);
        pho("dZ", PhonemeType.tS);
        pho("AI", PhonemeType.a, PhonemeType.i);
        pho("aU", PhonemeType.a1);
        pho("I", PhonemeType.i);
        pho("@", PhonemeType.a);
        pho("S", PhonemeType.SS);
        pho("N", PhonemeType.g);
        pho("Z", PhonemeType.tS);
        pho("h", PhonemeType.pause); // ?
        pho("OI", PhonemeType.o, PhonemeType.i);
        pho("?", PhonemeType.pause);
        pho("x", PhonemeType.r);// correspond to the german "ch"
        pho("a:", PhonemeType.a1);
        pho("o:", PhonemeType.o1);
        pho("e:", PhonemeType.e);
        pho("o~", PhonemeType.o);//correspond to french "on"
        pho("R", PhonemeType.r);//correspond to french "r"
        pho("H", PhonemeType.u);//correspond to french semi-vowel "hu" in "huit"
        pho("9", PhonemeType.E1);//correspond to french "eu" in "neuf"
        pho("9~", PhonemeType.E1);
        pho("e~", PhonemeType.e);
        pho("a~", PhonemeType.a);
        pho("0", PhonemeType.O1);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private OpenMaryConstants(){};

    /**
     * Converts a VIB's {@code Speech} object to MaryXML format.
     * @param s the {@code Speech}
     * @param lang the Mary's language-code
     * @return the corresponding MaryXML
     * @see #toMaryLang(java.lang.String, int) to get the Mary's language-code
     */
    //spike REVERIE pitch +50 rate +10
    public static String toMaryXML(Speech s, String lang){
        String maryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<maryxml version=\"0.4\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxmlns=\"http://mary.dfki.de/2002/MaryXML\"\nxml:lang=\""+lang+"\">\n<p><prosody rate=\"+0%\" pitch=\"+0%\" range=\"+0%\" volume=\"loud\">\n";
        for(Object o : s.getSpeechElements()){
            if(o instanceof String) {
                maryXML += (String)o + "\n";
            }
            if(o instanceof TimeMarker){
                TimeMarker tm = (TimeMarker)o;
                //skip the TimeMarker "start"
                if(tm.getName().equalsIgnoreCase("start")) {
                    continue;
                }
                //end of a pitch accent ?
                for(PitchAccent p : s.getPitchAccent()){
                    if(isref(s, tm, p.getEnd())){
                        maryXML += "</t>\n";
                        break;
                    }
                }

                //add the marker
                maryXML += "\t<mark name='"+tm.getName()+"'/>\n";

                //begin of a boubary
                for(Boundary b : s.getBoundaries()){
                    if(isref(s, tm, b.getStart())){
                        maryXML += "<boundary tone='"+BOUNDARY[b.getBoundaryType()]+"' duration='"+(int)(b.getDuration()*1000)+"'/>\n";
                        break;
                    }
                }
                //begin of a pitch accent
                for(PitchAccent p : s.getPitchAccent()){
                    if(isref(s, tm, p.getStart())){
                        maryXML += "<t accent='"+PITCHACCENT[p.getPitchAccentType()]+"'>\n";
                        break;
                    }
                }
            }
        }
        maryXML += "</prosody></p></maryxml>";
        return maryXML;
    }

    /**
     * Call the {@code CharacterManager} to get the Mary's language-code corresponding to the voice of the current character.<br/>
     * the parameter called is : {@code "MARY_"+version+"_"+language.substring(0, 2)+"_LANG"}<br/>
     * i.e. {@code MARY_4_EN_LANG} or {@code MARY_3_DE_LANG}<br/>
     * If no definition for this parameter is found, it returns the english language-code of the corresponding version.
     * @param language the language of the {@code Speech}
     * @param version the Mary's version
     * @return the Mary's language-code of the current character
     */
    public static String toMaryLang(String language, int version){
        String lang = CharacterManager.getValueString("MARY_"+version+"_"+language.substring(0, 2)+"_LANG");
        return lang.isEmpty() ? CharacterManager.getValueString("MARY_"+version+"_EN_LANG") : lang;
    }

    /**
     * Call the {@code CharacterManager} to get the Mary's voice corresponding to the current character.<br/>
     * the parameter called is : {@code "MARY_"+version+"_"+language.substring(0, 2)+"_VOICE"}<br/>
     * i.e. {@code MARY_4_EN_VOICE} or {@code MARY_3_DE_VOICE}<br/>
     * If no definition for this parameter is found, it returns the english voice of the corresponding version.
     * @param language the language of the {@code Speech}
     * @param version the Mary's version
     * @return the Mary's voice of the current character
     */
    public static String toMaryVoice(String language, int version){
        String voice = CharacterManager.getValueString("MARY_"+version+"_"+language.substring(0, 2)+"_VOICE");
        return voice.isEmpty() ? CharacterManager.getValueString("MARY_"+version+"_EN_VOICE") : voice;
    }

    /**
     * Returns the sequence of VIB's {@code phonenes} corresponding to the specified Mary's phoneme.
     * @param maryPhoneme the Mary's phoneme
     * @return a sequence of VIB's {@code phonemes}
     */
    public static PhonemeType[] convertPhoneme(String maryPhoneme){
        PhonemeType[] toReturn = correspondingPhonemes.get(maryPhoneme);
        if(toReturn==null){
            PhonemeType pho;
            try{
                pho = PhonemeType.valueOf(maryPhoneme);
            }catch(IllegalArgumentException iae){
                Logs.warning(OpenMaryConstants.class.getName()+" unknown phoneme : "+maryPhoneme);
                pho = PhonemeType.pause; //default value ?
            }
            toReturn = new PhonemeType[] {pho};
        }
        return toReturn;

    }

    /**
     * function to add corresponding phonemes in the map
     * @param maryPhoneme the Mary's phoneme
     * @param phoneme the VIB's phoneme
     */
    private static void pho(String maryPhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.put(maryPhoneme, phonemes);
    }
    /**
     * function to add corresponding phonemes in the map<br/>
     * used when a Mary's one correspond to a sequence of two VIB's phonemes
     * @param maryPhoneme the Mary's phoneme
     * @param phoneme1 first VIB's phoneme
     * @param phoneme2 second VIB's phoneme
     */
    private static void pho(String maryPhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
        PhonemeType[] phonemes = {phoneme1, phoneme2};
        correspondingPhonemes.put(maryPhoneme, phonemes);
    }

    /**
     * Check if a {@code TimeMarker} refer to an other {@code TimeMarker} in the speech.
     * @param s the {@code Speech} object where the speech {@code TimeMarker} comes from.
     * @param speechTM the speech {@code TimeMarker}.
     * @param other the {@code TimeMarker} to check.
     * @return {@code true} if the other {@code TimeMarker} refer to the speech {@code TimeMarker}, {@code false} oherwise.
     */
    private static boolean isref(Speech s, TimeMarker speechTM, TimeMarker other){
        SynchPoint sp = other.getFirstSynchPointWithTarget();
        if(sp==null) {
            return false;
        }
        String targetName = sp.getTargetName();
        String sourceName = targetName.substring(0,targetName.indexOf(':'));
        if(s.getId().equalsIgnoreCase(sourceName)){
            String name = targetName.substring(targetName.indexOf(':')+1);
            if(speechTM.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
