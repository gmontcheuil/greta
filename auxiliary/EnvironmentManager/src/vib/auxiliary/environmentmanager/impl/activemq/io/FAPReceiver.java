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
package vib.auxiliary.environmentmanager.impl.activemq.io;

import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.fap.AbstractFAPReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.auxiliary.environmentmanager.util.Toolbox;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;

/**
 *
 * @author Brice Donval
 */
public class FAPReceiver extends AbstractFAPReceiver implements ConnectionListener {

    private vib.auxiliary.activemq.semaine.FAPReceiver fapReceiver;

    public FAPReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        fapReceiver = new vib.auxiliary.activemq.semaine.FAPReceiver(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        fapReceiver.addConnectionListener(this);
        Toolbox.log(environmentServer, mpegAnimatable, "++ FAPReceiver is initialized with the values below :\n  - Host = \"" + fapReceiver.getHost() + "\"\n  - Port = \"" + fapReceiver.getPort() + "\"\n  - Topic = \"" + fapReceiver.getTopic());
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_FAP_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                              IFAPReceiver                              */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return fapReceiver.getHost();
    }

    @Override
    public void setHost(String host) {
        fapReceiver.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** FAPReceiver Host has changed with the value below :\n  - Host = \"" + fapReceiver.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return fapReceiver.getPort();
    }

    @Override
    public void setPort(String port) {
        fapReceiver.setPort(port);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** FAPReceiver Port has changed with the value below :\n  - Port = \"" + fapReceiver.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        fapReceiver.stopConnection();
        fapReceiver = null;
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "-- FAPReceiver is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                            FAPFrameEmitter                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapfp) {
        fapReceiver.addFAPFramePerformer(fapfp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapfp) {
        fapReceiver.removeFAPFramePerformer(fapfp);
    }

    /* ---------------------------------------------------------------------- */
    /*                           ConnectionListener                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onConnection() {
        hasConnected();
    }

    @Override
    public void onDisconnection() {
        hasDisconnected();
    }

}
