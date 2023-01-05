/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.enocean.internal.eep.A5_10;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_10_06_ELTAKO extends A5_10 {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // private Future<?> timeOut = null;
    public A5_10_06_ELTAKO() {
        super();
    }

    public A5_10_06_ELTAKO(ERP1Message packet) {
        super(packet);
    }

    protected int getUnscaledTemperatureValue() {
        return getDB_1Value();
    }

    protected int getUnscaledSetPointValue() {
        return getDB_2Value();
    }

    protected double getUnscaledTemperatureMin() {
        return 255;
    }

    protected double getUnscaledTemperatureMax() {
        return 0;
    }

    protected double getUnscaledSetPointMin() {
        return 0;
    }

    protected double getUnscaledSetPointMax() {
        return 255;
    }

    protected double getScaledTemperatureMin() {
        return 0;
    }

    protected double getScaledTemperatureMax() {
        return 40;
    }

    protected double getScaledSetPointMin() {
        return 0;
    }

    protected double getScaledSetPointMax() {
        return 40;
    }

    static final byte PriorityOff = 0x0A;
    static final byte PriorityOn = 0x08;
    static final byte NightReduction0 = 0x00;
    static final byte NightReduction1 = 0x06;
    static final byte NightReduction2 = 0x0C;
    static final byte NightReduction3 = 0x13;
    static final byte NightReduction4 = 0x19;
    static final byte NightReduction5 = 0x1F;

    private byte getTsp(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_SETPOINT);
        int carrentValue = (int) Math.floor(Double.parseDouble(current.toString()));
        return (byte) (6.375 * carrentValue);
    }

    private byte getBlc(Function<String, State> getCurrentStateFunc) {
        State current = getCurrentStateFunc.apply(CHANNEL_BUTTON_LOCK);
        if ((current != null) && (current instanceof OnOffType)) {
            OnOffType state = current.as(OnOffType.class);

            if (state != null) {
                return state.equals(OnOffType.ON) ? PriorityOn : PriorityOff;
            }
        }

        return PriorityOff;
    }

    /*
     * private byte getDso(Function<String, State> getCurrentStateFunc) {
     * State current = getCurrentStateFunc.apply(CHANNEL_DISPLAY_ORIENTATION);
     * // logger.info("current display :{}", current);
     * if ((current != null) && (current instanceof DecimalType)) {
     * DecimalType state = current.as(DecimalType.class);
     * int carrentValue = (int) Math.floor(Double.parseDouble(current.toString()));
     * if (state != null) {
     * switch (carrentValue) {
     * case 0:
     * return NightReduction0;
     * case 1:
     * return NightReduction1;
     * case 2:
     * return NightReduction2;
     * case 3:
     * return NightReduction3;
     * case 4:
     * return NightReduction4;
     * case 5:
     * return NightReduction5;
     *
     * }
     *
     * }
     * }
     *
     * return NightReduction0; // 0°
     * }
     */
    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (CHANNEL_SETPOINT.equals(channelId) || CHANNEL_BUTTON_LOCK.equals(channelId)) {
            // || CHANNEL_DISPLAY_ORIENTATION.equals(channelId)
            byte db3 = (byte) (0x00);// getDso(getCurrentStateFunc);
            byte db2 = getTsp(getCurrentStateFunc);
            byte db1 = (byte) (0x00);
            byte db0 = getBlc(getCurrentStateFunc);
            setData(db3, db2, db1, db0);
            /*
             * logger.info("1current display :{}", db2);
             * EnOceanChannelRoomOperatingPanel c = config.as(EnOceanChannelRoomOperatingPanel.class);
             * if (c.eltakoresetGfvs) {
             * scheduler.schedule(() -> {
             * setData(db3, (byte) (0x00), db1, db0); // Clear GFVS
             * logger.info("2current display :{}", db2);
             * }, 50, TimeUnit.MILLISECONDS);
             * 
             * }
             */
            return;

        }
        if (CHANNEL_TEACHINCMD.equals(channelId)) {
            byte db3 = (byte) (0x40);
            byte db2 = (byte) (0x30);
            byte db1 = (byte) (0x0D);
            byte db0 = (byte) (0x85);
            setData(db3, db2, db1, db0);
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_DISPLAY_ORIENTATION:
                return getBit(getDB_3(), 0) ? OnOffType.OFF : OnOffType.ON;

            case CHANNEL_SETPOINT:
                double scaledSetPoitn = getScaledSetPointMin()
                        + ((getUnscaledSetPointValue() * (getScaledSetPointMax() - getScaledSetPointMin()))
                                / (getUnscaledSetPointMax() - getUnscaledSetPointMin()));
                return new QuantityType<>(scaledSetPoitn, SIUnits.CELSIUS);

            case CHANNEL_TEMPERATURE:
                double scaledTemp = getScaledTemperatureMin()
                        - (((getUnscaledTemperatureMin() - getUnscaledTemperatureValue())
                                * (getScaledTemperatureMin() - getScaledTemperatureMax()))
                                / getUnscaledTemperatureMin());
                return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);

            /*
             * Does not send status of this
             * case CHANNEL_BUTTON_LOCK:
             * return getBit(getDB_0Value(), 2) ? OnOffType.ON : OnOffType.OFF;
             */

        }

        return UnDefType.UNDEF;
    }
}
