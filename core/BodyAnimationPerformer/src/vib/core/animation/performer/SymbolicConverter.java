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
package vib.core.animation.performer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vib.core.animation.IKSkeletonParser;
import vib.core.animation.Joint;
import vib.core.animation.Skeleton;
import vib.core.animation.body.Arm;
import vib.core.animation.body.ArmIKsolver;
import vib.core.animation.body.Head;
import vib.core.animation.body.LowerBody;
import vib.core.animation.body.Shoulder;
import vib.core.animation.body.Torso;
import vib.core.keyframes.GestureKeyframe;
import vib.core.keyframes.HeadKeyframe;
import vib.core.keyframes.ShoulderKeyframe;
import vib.core.keyframes.TorsoKeyframe;
import vib.core.repositories.HandShape;
import vib.core.repositories.HandShapeLibrary;
import vib.core.repositories.HeadIntervals;
import vib.core.repositories.HeadLibrary;
import vib.core.repositories.TorsoIntervals;
import vib.core.repositories.TorsoLibrary;
import vib.core.signals.SpineDirection;
import vib.core.signals.gesture.SymbolicPosition;
import vib.core.signals.gesture.TouchPosition;
import vib.core.signals.gesture.TrajectoryDescription;
import vib.core.signals.gesture.UniformPosition;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.enums.Side;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

/*
 * enum SymbolicPositionType {
 *
 * XEP, XP, XC, XCC, XOppC, YUpperEP, YUpperP, YUpperC, YCC, YLowerC, YLowerP,
 * YLowerEP, ZNear, ZMiddle, ZFar; }
 */
/**
 * converter all the value from file into each body parts
 *
 * @author Jing Huang
 */
public class SymbolicConverter implements CharacterDependent {

    ArmIKsolver _leftIK = new ArmIKsolver();
    ArmIKsolver _rightIK = new ArmIKsolver();
    Skeleton _skeleton = new Skeleton("agent");
    Skeleton _skeleton_original = new Skeleton("agent");
    HashMap<String, Skeleton> _chains = new HashMap<String, Skeleton>();
    private String xmlFile;
    private String touchFile;
    XMLTree _tree;
    RestPoseFactory _restPoseFactory = new RestPoseFactory();
    HeadIntervals _headIntervals = HeadLibrary.getGlobalLibrary().getHeadIntervals();
    TorsoIntervals _torsoIntervals = TorsoLibrary.getGlobalLibrary().getTorsoIntervals();
    HandShapeLibrary _handshapelib = new HandShapeLibrary();
    Vec3d gestureSpace_LeftOffset = new Vec3d();
    Vec3d gestureSpace_RightOffset = new Vec3d();
    Vec3d gestureSpace_LeftScale = new Vec3d();
    Vec3d gestureSpace_RightScale = new Vec3d();
    double meanArmLength = 1;
    double openessFactor = 0.0362f;//based on poppy's skeleton

    HashMap<String, String> _touchPosition = new HashMap<String, String>();
    HashMap<String, Vec3d> _touchPosOffset = new HashMap<String, Vec3d>();
    HashMap<String, Vec3d> _touchRotOffset = new HashMap<String, Vec3d>();
    double _scaleFactorX = 1f;
    double _scaleFactorY = 1f;
    double _scaleFactorZ = 1f;

    public SymbolicConverter() {
        CharacterManager.add(this);
        xmlFile = CharacterManager.getValueString("IK_SKELETON");
        touchFile = CharacterManager.getValueString("TOUCHPOINT");
        reloadData();
    }

    @Override
    public void onCharacterChanged() {
        xmlFile = CharacterManager.getValueString("IK_SKELETON");
        touchFile = CharacterManager.getValueString("TOUCHPOINT");
        reloadData();
    }

    public void reloadData() {
        setSkeleton(xmlFile);
        _restPoseFactory.reloadData();
        _torsoIntervals.reloadData();
        _headIntervals.reloadData();
        loadData();
    }

    private void setSkeleton(String filename) {
        IKSkeletonParser p = new IKSkeletonParser();
        boolean re = p.loadFile(filename);
        if (re) {
            p.readSkeletonInfo(_skeleton_original);
            _skeleton = _skeleton_original.clone();

            gestureSpace_LeftOffset = new Vec3d(
                    _skeleton_original.getJoint("HumanoidRoot").getWorldPosition().x(),
                    _skeleton_original.getJoint("l_elbow").getWorldPosition().y(),
                    _skeleton_original.getJoint("l_shoulder").getWorldPosition().z());
            gestureSpace_RightOffset = new Vec3d(
                    _skeleton_original.getJoint("HumanoidRoot").getWorldPosition().x(),
                    _skeleton_original.getJoint("r_elbow").getWorldPosition().y(),
                    _skeleton_original.getJoint("r_shoulder").getWorldPosition().z());

            double leftArmLength = Vec3d.substraction(_skeleton_original.getJoint("l_shoulder").getWorldPosition(), _skeleton_original.getJoint("l_wrist").getWorldPosition()).length();
            double rightArmLength = Vec3d.substraction(_skeleton_original.getJoint("r_shoulder").getWorldPosition(), _skeleton_original.getJoint("r_wrist").getWorldPosition()).length();

            gestureSpace_LeftScale = new Vec3d(leftArmLength, leftArmLength, leftArmLength);
            gestureSpace_RightScale = new Vec3d(-rightArmLength, rightArmLength, rightArmLength);

            meanArmLength = (leftArmLength + rightArmLength) / 2;
            _leftIK.setOriginal(_skeleton.getJoint("l_shoulder").getPosition(), _skeleton.getJoint("l_elbow").getPosition(), _skeleton.getJoint("l_wrist").getPosition(), "left");
            _rightIK.setOriginal(_skeleton.getJoint("r_shoulder").getPosition(), _skeleton.getJoint("r_elbow").getPosition(), _skeleton.getJoint("r_wrist").getPosition(), "right");
        } else {
            System.out.println("HumanAgent class  : skeleton file not loaded");

        }
    }

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public Skeleton getOriginalSkeleton() {
        return _skeleton_original;
    }

    public Skeleton getSkeletonChain(String name) {
        return _chains.get(name);
    }

    void loadData() {
        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        try {
            _tree = xmlparser.parseFile(touchFile);
            XMLTree rootNodeBase = _tree.getRootNode();
            List<XMLTree> listNode = rootNodeBase.getChildrenElement();
            for (int inod = 0; inod < listNode.size(); inod++) {
                XMLTree node = listNode.get(inod);
                if (node.getName().equalsIgnoreCase("touchpoint")) {
                    _touchPosition.put(node.getAttribute("id"), node.getAttribute("reference"));
                    List<XMLTree> offsetNodes = node.getChildrenElement();
                    for (int ioffs = 0; ioffs < offsetNodes.size(); ioffs++) {
                        XMLTree off = offsetNodes.get(ioffs);
                        if (off.getName().equalsIgnoreCase("posOffset")) {
                            _touchPosOffset.put(node.getAttribute("id"), new Vec3d(Double.valueOf(off.getAttribute("x")), Double.valueOf(off.getAttribute("y")), Double.valueOf(off.getAttribute("z"))));
                        }
                        if (off.getName().equalsIgnoreCase("rotOffset")) {
                            _touchRotOffset.put(node.getAttribute("id"), new Vec3d(Double.valueOf(off.getAttribute("x")), Double.valueOf(off.getAttribute("y")), Double.valueOf(off.getAttribute("z"))));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Problem while opening TouchPoint description file");
        }
    }

    public RestPoseFactory getRestPoseFactory() {
        return _restPoseFactory;
    }

    public void setRestPoseFactory(RestPoseFactory _restPoseFactory) {
        this._restPoseFactory = _restPoseFactory;
    }

    public HashMap<String, Quaternion> getHandShape(String handshape, Side side) {
        //System.out.println(shape);
        HandShape hs = _handshapelib.get(handshape);
        HashMap<String, Quaternion> qm = new HashMap<String, Quaternion>();
        if (hs == null) {
            System.out.println("class SymbolicConverter: " + handshape + " does not exist ");
            return qm;
        }
        for (String name : hs.getJointNames()) {
            Vec3d r = hs.getJoint(name);
            Quaternion rq = new Quaternion();
            if (side == Side.LEFT) {
                rq.fromEulerXYZByAngle(r.x(), -r.y(), -r.z());
                String newname = name.replace("r_", "l_");
                qm.put(newname, rq);
            } else {
                rq.fromEulerXYZByAngle(r.x(), r.y(), r.z());
                qm.put(name, rq);
            }

        }
        return qm;
    }

    public HeadIntervals getHeadIntervals() {
        return _headIntervals;
    }

    public TorsoIntervals getTorsoIntervals() {
        return _torsoIntervals;
    }

    public Shoulder getShoulder(ShoulderKeyframe keyframe) {
        Shoulder shoulder = new Shoulder();
        shoulder.setTime((double) keyframe.getOffset());
        String side = keyframe.getSide();
        shoulder.setSide(side);
        double front = keyframe.getFront();
        double up = keyframe.getUp();
        shoulder.compute(front, up);
        /*
         * if (side.equalsIgnoreCase("LEFT")) { } else if
         * (side.equalsIgnoreCase("RIGHT")) { }
         */
        return shoulder;
    }

    public Arm getArm(GestureKeyframe gesture) {
        Arm arm = new Arm();
        
        arm.setIsStrokeEnd(gesture.getPhaseType().equals("stroke_end"));
        arm.setTime((double) gesture.getOffset());
        arm.setSide(gesture.getSide());
        arm.setExpressivityParameters(gesture.getParameters());
        arm.setOpenness(gesture.getHand().getOpenness() * meanArmLength * openessFactor);
        TrajectoryDescription trajectory = gesture.getTrajectoryType();
        arm.setTraj(trajectory);
        double pwr = arm.getExpressivityParameters().pwr;

        /*
         * if (pwr >= 0.1) { if (pwr < 0.3) { arm.setFunction(new
         * EaseInOutSine()); } else if (pwr < 0.5) { arm.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.7) { arm.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.95) { arm.setFunction(new
         * EaseOutBack()); //overshoot } else if (pwr >= 0.95) {
         * arm.setFunction(new EaseOutBounce()); //rebounce } }
         */
        HashMap<String, Quaternion> result = getHandShape(gesture.getHand().getHandShape(), gesture.getSide());

        //hand shapes for rest poses
        if (gesture.isIsScript()) {
            arm.setRestPosName(gesture.getScriptName());
            if (gesture.getSide() == Side.LEFT) {
                result = getHandShape(_restPoseFactory.getLeftHandShape(arm.getRestPosName()), Side.LEFT);
            } else {
                result = getHandShape(_restPoseFactory.getRightHandShape(arm.getRestPosName()), Side.RIGHT);
            }
        }

        arm.addRotations(result);
        //arm.setHand(getHand(gesture));
        if (gesture.getHand().getPosition() instanceof SymbolicPosition) {
//            SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
            if (gesture.isIsScript()) {// || pos.getStringHorizontalLocation().equals("XR") || pos.getStringHorizontalLocation().equals("XRF")) {
                //gesture.getScriptName()   i can not get script per hand,~~~ so i can not apply other restpos, i dont know why he is doing that,  rest pos needs to define per part
                arm.setRestPosName(gesture.getScriptName());
                if (gesture.getSide() == Side.LEFT) {
                    HashMap<String, Quaternion> jf = _restPoseFactory.getLeftHandPose(arm.getRestPosName());
                    //System.out.println(arm.getRestPosName()+"left :");
                    for (String name : jf.keySet()) {
                        Quaternion localrotation = jf.get(name);
                        Vec3d euler = localrotation.getEulerAngleXYZByAngle();
                        //System.out.println("  "+name);
                        //System.out.println("     "+euler);
                        arm.addRotation(name, localrotation);
                        Joint j = _skeleton.getJoint(name);
                        j.setLocalRotation(localrotation);
                        j.update();
                    }
                    _skeleton.update();

                    Joint j = _skeleton.getJoint("l_wrist");
                    arm.setTarget(new Vec3d(j.getWorldPosition()));
                    //System.out.println(" pos target "+j.getWorldPosition());
                    arm.setWrist(new Quaternion(), gesture.getHand().isWristOrientationGlobal());
                } else {
                    HashMap<String, Quaternion> jf = _restPoseFactory.getRightHandPose(arm.getRestPosName());
                    //System.out.println(arm.getRestPosName()+" right:");
                    for (String name : jf.keySet()) {
                        Quaternion localrotation = jf.get(name);
                        Vec3d euler = localrotation.getEulerAngleXYZByAngle();
                        //System.out.println("  "+name);
                        //System.out.println("     "+euler);
                        arm.addRotation(name, localrotation);
                        Joint j = _skeleton.getJoint(name);
                        j.setLocalRotation(localrotation);
                        j.update();
                    }
                    _skeleton.update();
                    Joint j = _skeleton.getJoint("r_wrist");
                    arm.setTarget(new Vec3d(j.getWorldPosition()));
                    arm.setWrist(new Quaternion(), gesture.getHand().isWristOrientationGlobal());
                }
                return arm;
            }
        }

        if (gesture.getSide() == Side.LEFT) {
            //IF TOUCH POSITION
            if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                Joint j = _skeleton.getJoint(_touchPosition.get(pos.getId()));
                arm.setTarget(Vec3d.addition(j.getWorldPosition(), _touchPosOffset.get(pos.getId())));
                arm.useTouchPoint(j.getName(), _touchPosOffset.get(pos.getId()));
                //arm.setPosition(new Vec3d(7,35,15));
            } else if (gesture.getHand().getPosition() instanceof UniformPosition) {
                UniformPosition pos = (UniformPosition) gesture.getHand().getPosition();
                arm.setTarget(new Vec3d(
                        (double) (pos.getX() * gestureSpace_LeftScale.x() + gestureSpace_LeftOffset.x()),
                        (double) (pos.getY() * gestureSpace_LeftScale.y() + gestureSpace_LeftOffset.y()),
                        (double) (pos.getZ() * gestureSpace_LeftScale.z() + gestureSpace_LeftOffset.z())));
            }
            arm.setWrist(gesture.getHand().getWristOrientation(), gesture.getHand().isWristOrientationGlobal());

            Quaternion globalwrist = null;
            if(arm.isGlobalOrientation()){
                globalwrist = arm.getWrist();
            }
            _leftIK.compute(arm.getTarget(), globalwrist, arm.getOpenness());
            ArrayList<Quaternion> rotations = _leftIK.getRotations();
            arm.addRotation("l_shoulder", rotations.get(0));
            arm.addRotation("l_elbow", rotations.get(1));
            if(arm.isGlobalOrientation()){
                arm.addRotation("l_wrist", rotations.get(2));
            }

        } else {
            //IF TOUCH POSITION
            if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                Joint j = _skeleton.getJoint(_touchPosition.get(pos.getId()));
                arm.setTarget(Vec3d.addition(j.getWorldPosition(), _touchPosOffset.get(pos.getId())));
                arm.useTouchPoint(j.getName(), _touchPosOffset.get(pos.getId()));
            } else if (gesture.getHand().getPosition() instanceof UniformPosition) {
                UniformPosition pos = (UniformPosition) gesture.getHand().getPosition();
                arm.setTarget(new Vec3d(
                        (double) (pos.getX() * gestureSpace_RightScale.x() + gestureSpace_RightOffset.x()),
                        (double) (pos.getY() * gestureSpace_RightScale.y() + gestureSpace_RightOffset.y()),
                        (double) (pos.getZ() * gestureSpace_RightScale.z() + gestureSpace_RightOffset.z())));
            }
            arm.setWrist(gesture.getHand().getWristOrientation(), gesture.getHand().isWristOrientationGlobal());
            Quaternion globalwrist = null;
            if(arm.isGlobalOrientation()){
                globalwrist = arm.getWrist();
            }
            _rightIK.compute(arm.getTarget(), globalwrist, arm.getOpenness());
            ArrayList<Quaternion> rotations = _rightIK.getRotations();
            arm.addRotation("r_shoulder", rotations.get(0));
            arm.addRotation("r_elbow", rotations.get(1));
             if(arm.isGlobalOrientation()){
                arm.addRotation("r_wrist", rotations.get(2));
            }

        }

        return arm;
    }

    public Torso getTorse(TorsoKeyframe t) {
        Torso torse = new Torso();
        torse.setTime((double) t.getOffset());
        torse.setExpressivityParameters(t.getParameters());
        //select function
        double pwr = torse.getExpressivityParameters().pwr;

        if (!t._rotations.isEmpty()) {
            torse.addRotations(t._rotations);
            return torse;
        }

        /*
         * if (pwr >= 0.1) { if (pwr < 0.3) { torse.setFunction(new
         * EaseInOutSine()); } else if (pwr < 0.5) { torse.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.7) { torse.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.95) { torse.setFunction(new
         * EaseOutBack()); //overshoot } else if (pwr >= 0.95) {
         * torse.setFunction(new EaseOutBounce()); //rebounce } }
         */
        Quaternion q = new Quaternion();
        if (t.verticalTorsion.flag == true) {
            if (t.verticalTorsion.direction == SpineDirection.Direction.LEFTWARD) {
                double v = t.verticalTorsion.value * _torsoIntervals.verticalL;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 1, 0), radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.verticalTorsion.value * _torsoIntervals.verticalR;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 1, 0), -radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        if (t.sagittalTilt.flag == true) {
            if (t.sagittalTilt.direction == SpineDirection.Direction.FORWARD) {
                double v = t.sagittalTilt.value * _torsoIntervals.sagittalF;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(1, 0, 0), radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.sagittalTilt.value * _torsoIntervals.sagittalB;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(1, 0, 0), -radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        if (t.lateralRoll.flag == true) {
            if (t.lateralRoll.direction == SpineDirection.Direction.LEFTWARD) {
                double v = t.lateralRoll.value * _torsoIntervals.lateralL;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 0, 1), -radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.lateralRoll.value * _torsoIntervals.lateralR;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 0, 1), radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        //torse.setRotation(q);
        if (t.collapse.flag == true) {
            String list[] = {"vt3", "vt5", "vt12", "vl5"};
            Quaternion each = Quaternion.slerp(new Quaternion(), q, 0.25f, true);
            Quaternion each2 = Quaternion.slerp(new Quaternion(), q, 0.5f, true);
            torse.addRotation("vt2", each2);
            torse.addRotation("vt5", q);
            torse.addRotation("vt12", q);
            torse.addRotation("vl1", each2);
//            for (String name : list) {
//                torse.addRotation(name, each);
//            }
        } else {
            torse.addRotation("vl5", q);
        }
        return torse;
    }

    public Head getHead(HeadKeyframe h) {
        Head head = new Head();
        head.setTime((double) h.getOffset());
        head.setExpressivityParameters(h.getParameters());

        //select function
       /*
         * double pwr = head.getExpressivityParameters().pwr;
         *
         * if (pwr >= 0.1) { if (pwr < 0.3) { head.setFunction(new
         * EaseInOutSine()); } else if (pwr < 0.5) { head.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.7) { head.setFunction(new
         * EaseOutQuad()); } else if (pwr < 0.95) { head.setFunction(new
         * EaseOutBack()); //overshoot } else if (pwr >= 0.95) {
         * head.setFunction(new EaseOutBounce()); //rebounce } }
         */
        double rx = 0;
        double ry = 0;
        double rz = 0;
        if (h.verticalTorsion.flag == true) {
            if (h.verticalTorsion.direction != null) {
                if (h.verticalTorsion.direction == SpineDirection.Direction.LEFTWARD) {
                    ry = Math.toRadians(_headIntervals.verticalLeftMax);
                } else {
                    ry = -Math.toRadians(_headIntervals.verticalRightMax);
                }
                ry *= h.verticalTorsion.value;
            }
        }
        if (h.sagittalTilt.flag == true) {
            if (h.sagittalTilt.direction != null) {
                if (h.sagittalTilt.direction == SpineDirection.Direction.BACKWARD) {
                    rx = -Math.toRadians(_headIntervals.sagittalUpMax);
                } else {
                    rx = Math.toRadians(_headIntervals.sagittalDownMax);
                }
                rx *= h.sagittalTilt.value;
            }
        }
        if (h.lateralRoll.flag == true) {
            if (h.lateralRoll.direction != null) {
                if (h.lateralRoll.direction == SpineDirection.Direction.LEFTWARD) {
                    rz = -Math.toRadians(_headIntervals.lateralLeftMax);
                } else {
                    rz = Math.toRadians(_headIntervals.lateralRightMax);
                }
                rz *= h.lateralRoll.value;
            }
        }
        //System.out.println("head....  " + q.angle() +"    "+q.axis());
        Quaternion q = new Quaternion(new Vec3d(0, 1, 0), (double) ry);
        q.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) rx));
        q.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) rz));

        head.setRotation(q);
        {
            Quaternion qvc1 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc1.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.7)));
            qvc1.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.1)));

            Quaternion qvc4 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc4.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.2)));
            qvc4.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.3)));

            Quaternion qvc7 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc7.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.1)));
            qvc7.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.6)));

            head.addRotation("vc1", qvc1);
            head.addRotation("vc4", qvc4);
            head.addRotation("vc7", qvc7);

        }
        return head;
    }

    public LowerBody getLowerBody() {
        return new LowerBody(0, new Vec3d(), _skeleton_original.clone());
    }

    public double getScaleFactorX() {
        return _scaleFactorX;
    }

    public void setScaleFactorX(double _scaleFactorX) {
        this._scaleFactorX = _scaleFactorX;
    }

    public double getScaleFactorY() {
        return _scaleFactorY;
    }

    public void setScaleFactorY(double _scaleFactorY) {
        this._scaleFactorY = _scaleFactorY;
    }

    public double getScaleFactorZ() {
        return _scaleFactorZ;
    }

    public void setScaleFactorZ(double _scaleFactorZ) {
        this._scaleFactorZ = _scaleFactorZ;
    }
}
