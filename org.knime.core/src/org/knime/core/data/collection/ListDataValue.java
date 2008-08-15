/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   15.08.2008 (ohl): created
 */
package org.knime.core.data.collection;

import org.knime.core.data.DataCell;

/**
 * Provides additionally access by index to the collection elements.
 *
 * @author ohl, University of Konstanz
 */
public interface ListDataValue extends CollectionDataValue {

    /**
     * Returns the cell at the specified position in the list.
     *
     * @param index the position of the element to return (first element has
     *            index zero).
     * @return the cell at the specified position in the list
     */
    public DataCell get(final int index);
}
