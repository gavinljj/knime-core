/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any quesions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * --------------------------------------------------------------------- *
 * 
 * History
 *   25.09.2006 (ohl): created
 */
package org.knime.core.node.defaultnodesettings;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * A settingsmodel for double default components.
 * 
 * @author ohl, University of Konstanz
 */
public class SettingsModelDouble extends SettingsModelNumber {

    private double m_value;

    private final String m_configName;

    /**
     * Creates a new object holding a double value.
     * 
     * @param configName the identifier the value is stored with in the
     *            {@link org.knime.core.node.NodeSettings} object
     * @param defaultValue the initial value
     */
    public SettingsModelDouble(final String configName,
            final double defaultValue) {
        if ((configName == null) || (configName == "")) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
        m_value = defaultValue;
        m_configName = configName;
    }

    /**
     * 
     * @see org.knime.core.node.defaultnodesettings.SettingsModel#createClone()
     */
    @SuppressWarnings("unchecked")
    SettingsModelDouble createClone() {
        return new SettingsModelDouble(m_configName, m_value);
    }

    /**
     * @see SettingsModel#getModelTypeID()
     */
    @Override
    String getModelTypeID() {
        return "SMID_double";
    }

    /**
     * @see SettingsModel#getConfigName()
     */
    @Override
    String getConfigName() {
        return m_configName;
    }

    /**
     * set the value stored to the new value.
     * 
     * @param newValue the new value to store.
     */
    public void setDoubleValue(final double newValue) {
        m_value = newValue;
    }

    /**
     * @return the current value stored.
     */
    public double getDoubleValue() {
        return m_value;
    }

    /**
     * Allows to set a new value by passing a string that will be parsed and, if
     * valid, set as new value.
     * 
     * @param newValueStr the new value to be set, as string representation
     */
    @Override
    void setNumberValueStr(final String newValueStr) {
        try {
            double tmp = Double.valueOf(newValueStr);
            setDoubleValue(tmp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid floating point number"
                    + " format.");
        }
    }

    /**
     * @see SettingsModelNumber#getNumberValueStr()
     */
    @Override
    String getNumberValueStr() {
        return Double.toString(getDoubleValue());
    }

    /**
     * @see SettingsModel
     *      #loadSettingsForDialog(org.knime.core.node.NodeSettingsRO,
     *      org.knime.core.data.DataTableSpec[])
     */
    @Override
    void loadSettingsForDialog(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        try {
            // use the current value, if no value is stored in the settings
            setDoubleValue(settings.getDouble(m_configName, m_value));
        } catch (IllegalArgumentException e) {
            // if the value is not accepted, keep the old value.
        } finally {
            // always notify the listeners. That is, because there could be an
            // invalid value displayed in the listener.
            notifyChangeListeners();
        }
    }

    /**
     * @see SettingsModel
     *      #saveSettingsForDialog(org.knime.core.node.NodeSettingsWO)
     */
    @Override
    void saveSettingsForDialog(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    /**
     * @see SettingsModel
     *      #validateSettingsForModel(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    void validateSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        double oldVal = m_value;

        try {
            // no default value, throw an exception instead
            setDoubleValue(settings.getDouble(m_configName));

        } catch (IllegalArgumentException iae) {
            // value not accepted
            throw new InvalidSettingsException(iae.getMessage());
        } finally {
            m_value = oldVal;
        }
    }

    /**
     * @see SettingsModel
     *      #loadSettingsForModel(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    void loadSettingsForModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            // no default value, throw an exception instead
            setDoubleValue(settings.getDouble(m_configName));

        } catch (IllegalArgumentException iae) {
            // value not accepted
            throw new InvalidSettingsException(iae.getMessage());
        }
    }

    /**
     * @see SettingsModel
     *      #saveSettingsForModel(org.knime.core.node.NodeSettingsWO)
     */
    @Override
    void saveSettingsForModel(final NodeSettingsWO settings) {
        settings.addDouble(m_configName, m_value);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " ('" + m_configName + "')";
    }
}
