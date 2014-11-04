package com.rfxcom.rfxtrx.homeeasy;

import com.rfxcom.rfxtrx.RFXtrx;
import com.rfxcom.rfxtrx.message.Lighting2;
import com.rfxcom.rfxtrx.message.MessageListener;
import com.rfxcom.rfxtrx.message.MessageWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:53
 * To change this template use File | Settings | File Templates.
 */
public class HomeEasy {

    private final RFXtrx agent;
    private final Lighting2.SubType subType;
    private final List<Callback> callbacks = new ArrayList<Callback>();

    private final MessageListener listener = new MessageListener() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof Lighting2) {
                Lighting2 lightingMessageWrapper = (Lighting2)messageWrapper;
                if(lightingMessageWrapper.getSubType() == subType) {
                    int id = lightingMessageWrapper.getHouseId();
                    byte unitCode = lightingMessageWrapper.getUnitCode();
                    switch(lightingMessageWrapper.getCommand()) {
                        case On:
                            for(Callback listener : callbacks)
                                listener.turnedOn(id, unitCode);
                            break;
                        case OnAll:
                            for(Callback listener : callbacks)
                                listener.turnedOnAll(id);
                            break;
                        case Off:
                            for(Callback listener : callbacks)
                                listener.turnedOff(id, unitCode);
                            break;
                        case OffAll:
                            for(Callback listener : callbacks)
                                listener.turnedOffAll(id);
                            break;
                        case Level:
                            for(Callback listener : callbacks)
                                listener.setLevel(id, unitCode, lightingMessageWrapper.getLevel());
                            break;
                        case LevelAll:
                            for(Callback listener : callbacks)
                                listener.setLevelAll(id, lightingMessageWrapper.getLevel());
                            break;
                    }
                }
            }
        }
    };

    public static HomeEasy forUK(RFXtrx agent) {
        return new HomeEasy(agent, Lighting2.SubType.AC);
    }

    public static HomeEasy forEU(RFXtrx agent) {
        return new HomeEasy(agent, Lighting2.SubType.HomeEasyEU);
    }

    public HomeEasy(RFXtrx agent, Lighting2.SubType subType) {
        this.agent = agent;
        this.subType = subType;
        this.agent.addListener(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        this.agent.removeListener(listener);
        super.finalize();
    }

    public void addCallback(Callback listener) {
        callbacks.add(listener);
    }

    public void removeCallback(Callback listener) {
        callbacks.remove(listener);
    }

    private void sendCommand(int houseId, byte unitCode, Lighting2.Command command, byte level) throws IOException {
        agent.sendMessage(new Lighting2(subType, houseId, unitCode, command, level));
    }

    public void turnOn(int houseId, byte unitCode) throws IOException {
        sendCommand(houseId, unitCode, Lighting2.Command.On, (byte) 0x00);
    }

    public void turnOnAll(int houseId) throws IOException {
        sendCommand(houseId, (byte)0x00, Lighting2.Command.OnAll, (byte)0x00);
    }

    public void turnOff(int houseId, byte unitCode) throws IOException {
        sendCommand(houseId, unitCode, Lighting2.Command.Off, (byte)0x00);
    }

    public void turnOffAll(int houseId) throws IOException {
        sendCommand(houseId, (byte)0x00, Lighting2.Command.OffAll, (byte)0x00);
    }

    public void setLevel(int houseId, byte unitCode, byte level) throws IOException {
        sendCommand(houseId, unitCode, Lighting2.Command.Level, level);
    }

    public void setLevelAll(int houseId, byte level) throws IOException {
        sendCommand(houseId, (byte)0x00, Lighting2.Command.LevelAll, level);
    }

    public static interface Callback {
        void turnedOn(int houseId, byte unitCode);
        void turnedOnAll(int houseId);
        void turnedOff(int houseId, byte unitCode);
        void turnedOffAll(int houseId);
        void setLevel(int houseId, byte unitCode, byte level);
        void setLevelAll(int houseId, byte level);
    }
}