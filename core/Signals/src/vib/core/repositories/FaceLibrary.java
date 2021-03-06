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

import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.IniManager;
import vib.core.util.enums.Side;
import vib.core.util.parameter.ParameterSet;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Facelibrary.xml parser
 *
 * @author Radoslaw Niewiadomski
 */
public class FaceLibrary extends ParameterSet<AUExpression> implements CharacterDependent {

    public static final String CHARACTER_PARAMETER_FACELIBRARY = "FACELIBRARY";
    private static final String xsdFile = IniManager.getGlobals().getValueString("XSD_FACELIBRARY");
    public static FaceLibrary global_facelibrary = new FaceLibrary();

    public FaceLibrary() {
        //get the default Lexicon :
        super(CharacterManager.getDefaultValueString(CHARACTER_PARAMETER_FACELIBRARY));

        //load additionnal Lexicon :
        for (String filename : CharacterManager.getAllValuesString(CHARACTER_PARAMETER_FACELIBRARY)) {
            addDefinition(filename);
        }

        //set the current Lexicon to use :
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_FACELIBRARY));

        //to be notify when the character change :
        CharacterManager.add(this);
    }

    @Override
    protected List<AUExpression> load(String definition) {
        ArrayList<AUExpression> facelibrary = new ArrayList<AUExpression>();

        XMLParser parser = XML.createParser();

        //XMLTree facialexpressions = parser.parseFileWithXSD(definition,xsdFile);
        XMLTree facialexpressions = parser.parseFile(definition);

        if (facialexpressions != null) {
            for (XMLTree facialexpression : facialexpressions.getChildrenElement()) {

                if (facialexpression.isNamed("expression")) {

                    //create FLE
                    AUExpression flexpression = new AUExpression(facialexpression.getAttribute("instance"), facialexpression.hasAttribute("class") ? facialexpression.getAttribute("class") : "faceexp");

                    //load
                    for (XMLTree signal : facialexpression.getChildrenElement()) {
                        if (signal.isNamed("action")) {
                            AUItem auitem = new AUItem(signal.getAttribute("name"), signal.hasAttribute("intensity") ? signal.getAttributeNumber("intensity") : 1.0d, signal.hasAttribute("side") ? Side.valueOf(signal.getAttribute("side").toUpperCase()) : Side.BOTH);
                            flexpression.add(auitem); //adds the signal and constructs combinations
                        }//end if
                    }//end for

                    facelibrary.add(flexpression);

                }//end if expression
                else { //not expression so what else??
                    continue;
                }
            }
        }//empty

        return facelibrary;
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_FACELIBRARY));
    }

    @Override
    public void save(String definition, List<AUExpression> expressions) {
        Collections.sort(expressions, new Comparator<AUExpression>() {

            @Override
            public int compare(AUExpression o1, AUExpression o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getParamName(), o2.getParamName());
            }
        });

        XMLTree faceLibraryTree = XML.createTree("facelibrary");

        for (AUExpression expression : expressions) {
            XMLTree expressionTree = faceLibraryTree.createChild("expression");
            expressionTree.setAttribute("instance", expression.getInstanceName());
            expressionTree.setAttribute("class", expression.getType());
            for(AUItem au : expression.getActionUnits()){
                if(au.getIntensity()>0){
                    XMLTree action = expressionTree.createChild("action");
                    action.setAttribute("name", au.getAU());
                    if(au.getIntensity()<1){
                        action.setAttribute("intensity", ""+au.getIntensity());
                    }
                    if(au.getSide() != Side.BOTH){
                        action.setAttribute("side", au.getSide().name());
                    }
                }
            }
        }
        faceLibraryTree.save(definition);
    }
}
