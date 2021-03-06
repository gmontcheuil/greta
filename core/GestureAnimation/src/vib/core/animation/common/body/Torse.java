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
package vib.core.animation.common.body;

import vib.core.animation.common.Frame.ExtendedKeyFrame;
import vib.core.animation.common.Skeleton;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Torse extends ExtendedKeyFrame {
    Skeleton _skeleton;
    Quaternion r = new Quaternion();

    public Torse(double time) {
        super(time);
    }
    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public void setSkeleton(Skeleton _skeleton) {
        this._skeleton = _skeleton;
    }

    public void setRotation(Quaternion q) {
        r = new Quaternion(q);
    }

    public Quaternion getRotation() {
        return r;
    }

    public void pointToDirection(Vec3d direction) {
        Vec3d original = new Vec3d(0, 0, 1);
        Vec3d n = Vec3d.cross3(original, direction);
        double cosTheta = original.dot3(direction);
        cosTheta = 1 < cosTheta ? 1 : cosTheta;
        double angle = (double) java.lang.Math.acos(cosTheta);
        Quaternion global = new Quaternion(n, angle);

    }

    @Override
    public Torse clone() {
        return (Torse) super.clone();
    }
}
