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
package vib.auxiliary.BvhMocap;

import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;


/**
 *
 * @author Nesrine Fourati
 */
public class BvhMocap {

    /**
     * @param args the command line arguments
     */
            public static File LoadFile()
    {
            FileSystemView vueSysteme = FileSystemView.getFileSystemView();
            File defaut = vueSysteme.getDefaultDirectory();

            JFileChooser defautChooser = new JFileChooser(defaut);
            defautChooser.showOpenDialog(null);
            File file = defautChooser.getSelectedFile();
            return file;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        // TODO code application logic here

        // Generate a BAP file from a bvh file

       // File file=LoadFile();
       // String bvhfilepath= file.getPath();
        //System.out.println(bvhfilepath);
        //String bvhfilename=file.getName();
               String bvhpath="C:\\Users\\fourati\\Documents\\MotionCapture Database\\Janina\\Anger\\Simple Walk\\Action_repetitions\\";
      String bvhname="Ag1SW_Janina";
//
        BvhReader bvhread=new BvhReader(bvhpath + bvhname+".bvh");
        Bvh bvh = bvhread.JFTableBasedBvhCreator();
        AllJointFramesTable jointframestable = bvh.GetAllJointFramesTable();
       //System.out.println(jointframestable.GetNbFrame());
       SkeletonRelatedPositions SekeltonRelatedPos=new SkeletonRelatedPositions(jointframestable);
        SekeltonRelatedPos.CreateSkeletonRelatedPositions();
        AllJointFramesTable newjointframestable=SekeltonRelatedPos.GetAllJointFramesTable();
        Quaternion q=new Quaternion(new Vec3d(0,1,0),(float) -0.5);
        //System.out.println(q.angle()+"   "+q.axis().get(1));
        System.out.println(newjointframestable.GetJFListfromDictName("r_acromioclavicular").GetJointFrameAt(0).PositionInSkeletonRootCoordSystem);




//        ArrayList<BAPFrame> bapframes=bvhread.BvhToBAPFrames();
//        BapAnimationConverter converter=new BapAnimationConverter();
//        BAPFrame bapframe=bapframes.get(bapframes.size()-1);



        //converter.BapOutput(bapframes, "C:/Users/fourati/Documents/Bap files/", bvhfilename.split("[.]")[0], "____.bap", "0.0 xxx 25 1\n");

       // Bvh bvh=bvhread.BvhCreator();
//       ArrayList<Motion> molist= bvh.GetMotionList();
//      molist.get(9).DisplayEulerAngle(1);
        //bvh.JointsDisplay();

      // bvh.FillJointFramesTable();
//       AllJointFramesTable table=bvh.GetAllJointFramesTable();
//       ArrayList<JointFramesList> list=table.GetJointFramesTable();
//       //String[] Jointnames=table.GetJointNamesList();
//       System.out.println(list.get(0).JointName);


    }
}
