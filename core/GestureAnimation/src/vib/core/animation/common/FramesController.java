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
package vib.core.animation.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import vib.core.animation.common.Frame.JointFrame;
import vib.core.animation.common.Frame.KeyFrame;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.animation.mpeg4.bap.BAPType;
import vib.core.animation.mpeg4.bap.JointType;
import vib.core.util.Constants;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.time.Timer;
/**
 *  streaming BAP frame from FramesGenerato  to BAPFramesPerformer
 * @author Jing Huang
 */
public class FramesController extends Thread implements BAPFramesEmitter, FramesReceiver {

    ArrayList<BAPFramesPerformer> _bapframesPerformer = new ArrayList<BAPFramesPerformer>();
    LinkedList<KeyFrame> _framesList = new LinkedList<KeyFrame>();
    BAPFrame _bframe = new BAPFrame();
    private boolean requestStop = false;
    int numberFramesPerSend = 30;

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer performer) {
        if(performer!=null) {
            _bapframesPerformer.add(performer);
        }
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer performer) {
        _bapframesPerformer.remove(performer);
    }

    @Override
    public void run() {
        while (!requestStop) {

            long time = Timer.getTimeMillis();
            ArrayList<KeyFrame> frames = new ArrayList<KeyFrame>();
            synchronized (_framesList) {
                int nb = numberFramesPerSend < _framesList.size() ? numberFramesPerSend : _framesList.size();
                for (int i = 0; i < nb; ++i) {
                    frames.add(_framesList.poll());
                }
            }

            if (frames.size() > 0) {
                ArrayList<BAPFrame> bapframes = getBapFrames(frames);
                ID id = IDProvider.createID("FramesController");
                sendFrames(bapframes, id);
            }

            //The time can be controled, so we need the Timer.sleep function instead of the Thread.sleep function
            Timer.sleep(Math.max(1,Math.min(frames.size()*Constants.FRAME_DURATION_MILLIS,1000)-(Timer.getTimeMillis()-time)));
        }
    }
//
//    KeyFrame getFrameInfo(int i) {
//        if (_framesList.containsKey(i)) {
//            return _framesList.get(i);
//        }
//        KeyFrame f = new KeyFrame((double) i / (double) KeyFrame.getFramePerSecond());
//        _framesList.put(i, f);
//        return f;
//    }
//
//    void cleanFrameInfoInList(int i) {
//        if (_framesList.containsKey(i)) {
//            _framesList.remove(i);
//        }
//    }

    /**
     *
     * @param frames container the rotation info : quaternions
     * @return bap frames object
     */
    public ArrayList<BAPFrame> getBapFrames(ArrayList<KeyFrame> frames) {
        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();
        //int count = (int)(Timer.getTimeMillis()/ 40 );
        for (KeyFrame frame : frames) {
            BAPFrame bapframe = getBapFrame(frame, (int)(frame.getTime()*Constants.FRAME_PER_SECOND + 0.5));
            bapframes.add(bapframe);
        }
        return bapframes;
    }

    /**
     *
     * @param info
     * @param index
     * @return the {@code BAPFrame} at the specified index
     */
    public BAPFrame getBapFrame(KeyFrame info, int index) {
        BAPFrame bf = _bframe.clone();
        bf.setFrameNumber(index);
        HashMap<String, JointFrame> results = info.getJointFrames();
        Iterator iterator = results.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Quaternion q = results.get(name)._localrotation;
            Vec3d angle = q.getEulerAngleXYZ();
            JointType joint = JointType.get(name);
            BAPType z = joint.rotationZ;
            BAPType y = joint.rotationY;
            BAPType x = joint.rotationX;
            bf.setRadianValue(z, angle.z());
            bf.setRadianValue(y, angle.y());
            bf.setRadianValue(x, angle.x());
        }
        _bframe = bf.clone();
        return bf;
    }

    @Override
    public void updateFramesInfoList(ArrayList<KeyFrame> frames, String requestId) {
        //reset();
        synchronized (_framesList) {
            _framesList.addAll(frames);
//            for (int i = 0; i < frames.size(); ++i) {
//                KeyFrame framepart = frames.get(i);
//                _framesList.add(framepart);
////                int count = framepart.getCountNumber();
////                // if (_count <= count) {
////                Frame currentFrame = getFrameInfo(count + _count);
////                currentFrame.addJointFrames(framepart.getJointFrames());
//                //}
//            }
//            Logs.error("frames sizes : "+frames.size()+" "+_framesList.size());
        }
    }

    public void reset() {
        synchronized (_framesList) {
            _framesList.clear();
        }
    }

    void sendFrames(List<BAPFrame> bapframes, ID requestId) {

        /*
        for(BAPFrame frame : bapframes ){
            for (int i=1;i<BAPConverter.getNumBAPs();i++){
            BAP bap = frame.getBapList().get(i);
                if (bap.getMask())
                    System.out.println(BAPType.values()[i]);
            }
        }
        */




        for (int i = 0; i < _bapframesPerformer.size(); ++i) {
            BAPFramesPerformer performer = _bapframesPerformer.get(i);
            performer.performBAPFrames(bapframes, requestId);
        }
    }

    /**
     *
     * @param frames container the rotation info : quaternions
     * @return bap string
     */
    public String getFrames(ArrayList<KeyFrame> frames) {

        StringBuffer bap = new StringBuffer();
        for (KeyFrame frame : frames) {
            BAPFrame bapframe = getBapFrame(frame, frame.getCountNumber());
            String out = bapframe.AnimationParametersFrame2String();
            bap.append(out);
        }
        return bap.toString();
    }

    public void requestStop() {
        requestStop = true;
    }
}
