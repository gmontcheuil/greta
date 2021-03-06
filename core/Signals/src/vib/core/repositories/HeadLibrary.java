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
package vib.core.repositories;

import java.util.ArrayList;
import java.util.List;
import vib.core.signals.HeadSignal;
import vib.core.signals.SpineDirection;
import vib.core.signals.SpinePhase;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.IniManager;
import vib.core.util.log.Logs;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie Pez
 */
public class HeadLibrary extends SignalLibrary<HeadSignal> implements CharacterDependent{

    private static String library_file_key; // key in a character ini file
    private static String xsd_file_key; // key in the global ini file
    private static final HeadLibrary globalLibrary; // the global head library

    static {
        library_file_key = "HEADGESTURES";
        xsd_file_key = "XSD_HEADGESTURES";
        globalLibrary = new HeadLibrary();
        CharacterManager.add(globalLibrary);
    }

    /**
     * @return The global {@code HeadLibrary}.
     */
    public static HeadLibrary getGlobalLibrary(){
        return globalLibrary;
    }

    private HeadIntervals intervals = new HeadIntervals();

    public HeadIntervals getHeadIntervals(){
        return intervals;
    }

    public HeadLibrary(){
        super(CharacterManager.getValueString(library_file_key));
        intervals = new HeadIntervals();
    }

    @Override
    protected List<SignalEntry<HeadSignal>> load(String headLibFile) {
        ArrayList<SignalEntry<HeadSignal>> lib = new ArrayList<SignalEntry<HeadSignal>>();
        XMLParser parser = XML.createParser();
        XMLTree headtree = parser.parseFileWithXSD(headLibFile, IniManager.getGlobals().getValueString(xsd_file_key));
        if (headtree != null) {
            for (XMLTree headxml : headtree.getChildrenElement()) {
                HeadSignal headSignal = null;
                //<editor-fold defaultstate="collapsed" desc="<head ... >">
                if (headxml.isNamed("head")) {
                    int repetition = 1;
                    if (!headxml.hasAttribute("id")) {
                        Logs.warning("Head ID is not found!");
                        continue;
                    }
                    headSignal = new HeadSignal(headxml.getAttribute("id"));
                    if (headxml.hasAttribute("lexeme")) {
                        headSignal.setLexeme(headxml.getAttribute("lexeme"));
                    }
                    if (headxml.hasAttribute("repetition")) {
                        repetition = (int) headxml.getAttributeNumber("repetition");
                    }
                    for (XMLTree phase : headxml.getChildrenElement()) {
                        SpinePhase headPhase = new SpinePhase(phase.getAttribute("type"), 0, 0);
                        for (XMLTree direction : phase.getChildrenElement()) {
                            SpineDirection headDirection = null;
                            if (direction.isNamed("SagittalTilt")) {
                                headDirection = headPhase.sagittalTilt;
                            }
                            if (direction.isNamed("VerticalTorsion")) {
                                headDirection = headPhase.verticalTorsion;
                            }
                            if (direction.isNamed("LateralRoll")) {
                                headDirection = headPhase.lateralRoll;
                            }
                            if(headDirection !=null){
                                headDirection.direction = SpineDirection.Direction.valueOf(direction.getAttribute("direction").toUpperCase());
                                headDirection.flag = true;
                                headDirection.valueMax = direction.hasAttribute("max") ?
                                        direction.getAttributeNumber("max") :
                                        1;
                                headDirection.valueMin = direction.hasAttribute("min") ?
                                        direction.getAttributeNumber("min") :
                                        0;
                            }
                        }
                        headSignal.getPhases().add(headPhase);
                    }
                    int sum = headSignal.getPhases().size();
                    for (int i = 0; i < repetition - 1; i++) {
                        for (int j = 0; j < sum; j++) {
                            headSignal.getPhases().add(new SpinePhase(headSignal.getPhases().get(j)));
                        }
                    }
                    if(headSignal.getPhases().size()==1){
                        //add one more to make a sustain
                        headSignal.getPhases().add(new SpinePhase(headSignal.getPhases().get(0)));
                    }
                    lib.add(new SignalEntry<HeadSignal>(headSignal.getId(), headSignal));
                }
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="<headDirectionShift ... >">
                if (headxml.isNamed("headDirectionShift")) {
                    if (!headxml.hasAttribute("id")) {
                        Logs.warning("headDirectionShift ID is not found!");
                        continue;
                    }
                    headSignal = new HeadSignal(headxml.getAttribute("id"));
                    headSignal.setDirectionShift(true);
                    SpinePhase headPhase = new SpinePhase("strokeEnd", 0, 0);
                    for (XMLTree direction : headxml.getChildrenElement()) {
                        SpineDirection headDirection = null;
                        if (direction.isNamed("SagittalTilt")) {
                            headDirection = headPhase.sagittalTilt;
                        }
                        if (direction.isNamed("VerticalTorsion")) {
                            headDirection = headPhase.verticalTorsion;
                        }
                        if (direction.isNamed("LateralRoll")) {
                            headDirection = headPhase.lateralRoll;
                        }
                        if (headDirection != null) {
                            headDirection.direction = SpineDirection.Direction.valueOf(direction.getAttribute("direction").toUpperCase());
                            headDirection.flag = true;
                            headDirection.valueMax = direction.hasAttribute("max") ?
                                    direction.getAttributeNumber("max") :
                                    1;
                            headDirection.valueMin = direction.hasAttribute("min") ?
                                    direction.getAttributeNumber("min") :
                                    0;
                            headDirection.value = headDirection.valueMax;
                        }
                    }
                    headSignal.getPhases().add(headPhase);
                }
                //</editor-fold>
                if(headSignal != null) {
                    lib.add(new SignalEntry<HeadSignal>(headSignal.getId(), headSignal));
                }
            }
        }
        return lib;
    }

    /**
     * Search for one {@code HeadSignal} that matches with a specified lexeme
     * @param lexeme the lexeme to search
     * @return the entry that contains the headSignal matching the lexeme if it is found. {@code null} if it is not found.
     */
    public SignalEntry<HeadSignal> findOneForLexeme(String lexeme){
        if(lexeme==null) {
            return null;
        }
        for(SignalEntry<HeadSignal> entry : getAll()){
            if(lexeme.equalsIgnoreCase(entry.getSignal().getLexeme())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Retreives the lexeme corresponding to a reference.<br/>
     * if there is no lexeme, {@code null} is returned.
     * @param reference the reference
     * @return the corresponding lexeme
     */
    public String getLexemeOf(String reference){
        SignalEntry<HeadSignal> entry = this.get(reference);
        if(entry!=null){
            return entry.getSignal().getLexeme();
        }
        else{
            return null;
        }
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(CharacterManager.getValueString(library_file_key));
        intervals.onCharacterChanged();
    }

    @Override
    protected void save(String definition, List<SignalEntry<HeadSignal>> list) {
        XMLTree headLibraryTree = XML.createTree("HeadGestures");
        headLibraryTree.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        headLibraryTree.setAttribute("xsi:noNamespaceSchemaLocation", "../../Common/Data/xml/HeadGestures.xsd");//TODO: put the relatve path between XSDFile and taget file

        for(SignalEntry<HeadSignal> entry : list){
            HeadSignal head = entry.getSignal();
            if(head.isDirectionShift()){
                XMLTree headShiftTree = headLibraryTree.createChild("headDirectionShift");
                headShiftTree.setAttribute("id", head.getId());
                fillPhaseTree(headShiftTree, head.getPhases().get(0));
            }
            else{
                XMLTree headTree = headLibraryTree.createChild("head");
                headTree.setAttribute("id", head.getId());
                headTree.setAttribute("lexeme", head.getLexeme());
                for(SpinePhase phase : head.getPhases()){
                    fillPhaseTree(headTree.createChild("KeyPoint"), phase);
                }
            }
        }

        headLibraryTree.save(definition);
    }

    private void fillPhaseTree(XMLTree phaseTree, SpinePhase phase){
        createDirectionTree(phaseTree, phase.lateralRoll, "LateralRoll");
        createDirectionTree(phaseTree, phase.sagittalTilt, "SagittalTilt");
        createDirectionTree(phaseTree, phase.verticalTorsion, "VerticalTorsion");
    }

    private void createDirectionTree(XMLTree phaseTree, SpineDirection headDirection, String name){
        if(headDirection.flag){
            XMLTree directionTree = phaseTree.createChild(name);
            directionTree.setAttribute("direction", headDirection.direction.name());
            directionTree.setAttribute("min", Double.toString(headDirection.valueMin));
            directionTree.setAttribute("max", Double.toString(headDirection.valueMax));
        }
    }

}
