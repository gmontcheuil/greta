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
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackEmitter;
import vib.core.feedbacks.CallbackPerformer;
import vib.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Angelo Cafaro
 */
public class BMLCallbacksReceiver extends TextReceiver implements CallbackEmitter {

    private ArrayList<CallbackPerformer> callbackPerformersfList;

    public BMLCallbacksReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.input.feedback.BML");
    }

    public BMLCallbacksReceiver(String host, String port, String topic) {
        super(host, port, topic);
        callbackPerformersfList = new ArrayList<CallbackPerformer>();
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        String callbackType = properties.get("feedback-type").toString();
        double callbackTime = Double.parseDouble(properties.get("feedback-time").toString()) / 1000;
        String requestId = properties.get("feedback-id").toString();
        
        Callback callback = new Callback(callbackType, callbackTime, IDProvider.createID(requestId));
        for (CallbackPerformer performer : callbackPerformersfList) {
            performer.performCallback(callback);
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.add(performer);
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.remove(performer);
    }
}
