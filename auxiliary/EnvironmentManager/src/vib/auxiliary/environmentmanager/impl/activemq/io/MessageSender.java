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

import java.util.Map;
import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.message.AbstractMessageSender;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.auxiliary.environmentmanager.util.Toolbox;

/**
 *
 * @author Brice Donval
 */
public class MessageSender extends AbstractMessageSender implements ConnectionListener {

    private vib.auxiliary.activemq.TextSender textSender;

    /* ---------------------------------------------------------------------- */

    public MessageSender(IEnvironmentServer environmentServer) {
        super(environmentServer);
        textSender = new vib.auxiliary.activemq.TextSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        textSender.addConnectionListener(this);
        Toolbox.log(environmentServer, "++ MessageSender is initialized with the values below :\n  - Host = \"" + textSender.getHost() + "\"\n  - Port = \"" + textSender.getPort() + "\"\n  - Topic = \"" + textSender.getTopic());
    }

    private String getTopic() {
        return getEnvironmentServer().isPrimary() ? ActiveMQConstants.Topic_Message_from_Primary : ActiveMQConstants.Topic_Message_from_Replica;
    }

    /* ---------------------------------------------------------------------- */
    /*                             IMessageSender                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return textSender.getHost();
    }

    @Override
    public void setHost(String host) {
        textSender.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** MessageSender Host has changed with the value below :\n  - Host = \"" + textSender.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return textSender.getPort();
    }

    @Override
    public void setPort(String port) {
        textSender.setPort(port);
        Toolbox.log(getEnvironmentServer(), "** MessageSender Port has changed with the value below :\n  - Port = \"" + textSender.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void send(String message, Map<String, String> details) {
        Map<String, Object> properties = (Map) details;
        textSender.send(message, properties);
        Toolbox.log(getEnvironmentServer(), "<- MessageSender is sending the message below :\n  - Message = \"" + message + "\"\n  - Details = " + properties);
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        textSender.stopConnection();
        textSender = null;
        Toolbox.log(getEnvironmentServer(), "-- MessageSender is destroyed.");
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
