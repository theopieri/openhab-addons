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
package org.openhab.binding.enocean.internal.eep.A5_09;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class A5_09 extends _4BSMessage {

    public A5_09(ERP1Message packet) {
        super(packet);
    }

    protected double getUnscaledTemperatureMin() {
        return 0;
    }

    protected double getUnscaledTemperatureMax() {
        return 255;
    }

    protected double getUnscaledCO2Min() {
        return 0;
    }

    protected double getUnscaledCO2Max() {
        return 255;
    }

    protected double getUnscaledHumidityMax() {
        return 200;
    }

    protected abstract double getScaledTemperatureMin();

    protected abstract double getScaledTemperatureMax();

    protected abstract double getScaledCO2Min();

    protected abstract double getScaledCO2Max();

    protected int getUnscaledTemperatureValue() {
        return getDB_1Value();
    }

    protected int getUnscaledCO2Value() {
        return getDB_2Value();
    }

    protected int getUnscaledHumidityValue() {
        return getDB_3Value();
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        if (channelId.equals(CHANNEL_TEMPERATURE)) {
            double scaledTemp = getScaledTemperatureMin()
                    + ((getUnscaledTemperatureValue() * (getScaledTemperatureMax() - getScaledTemperatureMin()))
                            / (getUnscaledTemperatureMax() - getUnscaledTemperatureMin()));
            return new QuantityType<>(scaledTemp, SIUnits.CELSIUS);
        } else if (channelId.equals(CHANNEL_HUMIDITY)) {
            return new DecimalType((getUnscaledHumidityValue() * 100.0) / getUnscaledHumidityMax());
        } else {
            if (channelId.equals(EnOceanBindingConstants.CHANNEL_AIRQUALITYVALUE1)) {
                double scaledCO2 = getScaledCO2Min()
                        + ((getUnscaledCO2Value() * (getScaledCO2Max() - getScaledCO2Min()))
                                / (getUnscaledCO2Max() - getUnscaledCO2Min()));
                return new QuantityType<>(scaledCO2, Units.PARTS_PER_MILLION);
                // return (new DecimalType(scaledCO2));
            }
        }
        return UnDefType.UNDEF;
    }
}
