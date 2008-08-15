/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
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
 *   Aug 11, 2008 (wiswedel): created
 */
package org.knime.core.data.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.data.container.BlobSupportDataRow;
import org.knime.core.data.container.BlobWrapperDataCell;
import org.knime.core.node.BufferedDataTable;

/**
 * A list of {@link DataCell} objects, which special treatment for
 * {@link BlobDataCell}. The implementation will keep blobs in special
 * {@link BlobWrapperDataCell} in order to allow for a possible garbage
 * collection (and deserializing when a blob is accessed.)
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class BlobSupportDataCellList implements Iterable<DataCell> {

    private final List<DataCell> m_cellList;

    private boolean m_containsBlobWrapperCells;

    private final DataType m_elementType;

    /**
     * Creates new instance based on a collection of data cells.
     *
     * @param coll The underlying collection (will be copied).
     *            {@link BlobDataCell} in this collection will be handled with
     *            care.
     */
    protected BlobSupportDataCellList(final Collection<DataCell> coll) {
        ArrayList<DataCell> cellList = new ArrayList<DataCell>(coll.size());
        DataType commonType = null;
        for (DataCell c : coll) {
            if (c == null) {
                throw new NullPointerException("List element must not be null");
            }
            DataType cellType;
            if (c instanceof BlobWrapperDataCell) {
                m_containsBlobWrapperCells = true;
                cellList.add(c);
                cellType =
                        DataType.getType(((BlobWrapperDataCell)c)
                                .getBlobClass());
            } else if (c instanceof BlobDataCell) {
                m_containsBlobWrapperCells = true;
                cellList.add(new BlobWrapperDataCell((BlobDataCell)c));
                cellType = c.getType();
            } else {
                cellList.add(c);
                cellType = c.getType();
            }
            if (!c.isMissing()) {
                if (commonType == null) {
                    commonType = cellType;
                } else {
                    commonType =
                            DataType.getCommonSuperType(commonType, cellType);
                }
            }
        }
        if (commonType == null) {
            m_elementType = DataType.getMissingCell().getType();
        } else {
            m_elementType = commonType;
        }
        m_cellList = Collections.unmodifiableList(cellList);
    }

    /**
     * @return the containsBlobs
     */
    public final boolean containsBlobWrapperCells() {
        return m_containsBlobWrapperCells;
    }

    /**
     * Returns the element at the specified position of the list. If it is a
     * blob wrapper cell, it is unwrapped and the contained data cell is
     * returned.
     *
     * @param index the index of the element to return
     * @return the element at the specified position.
     * @throws IndexOutOfBoundsException if the index is larger than the list is
     *             long
     */
    public DataCell get(final int index) {
        DataCell cell = m_cellList.get(index);
        if (cell instanceof BlobWrapperDataCell) {
            return ((BlobWrapperDataCell)cell).getCell();
        } else {
            return cell;
        }
    }

    /**
     * Returns the element at the specified position of the list. If the element
     * is a blob wrapper cell the wrapper will be returned (and not unwrapped).
     *
     * @param index the index of the element to return
     * @return the element at the specified position.
     * @throws IndexOutOfBoundsException if the index is larger than the list is
     *             long
     */
    public DataCell getWithBlobSupport(final int index) {
        return m_cellList.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<DataCell> iterator() {
        if (!containsBlobWrapperCells()) {
            return m_cellList.iterator();
        }
        return new DefaultBlobSupportDataCellIterator(m_cellList.iterator());
    }

    /**
     * @return the elementType
     */
    public DataType getElementType() {
        return m_elementType;
    }

    /** @return Size of the list. */
    public int size() {
        return m_cellList.size();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_cellList.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BlobSupportDataCellList)) {
            return false;
        }
        BlobSupportDataCellList o = (BlobSupportDataCellList)obj;
        return o.getElementType().equals(m_elementType)
                && o.m_cellList.equals(m_cellList);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return m_cellList.hashCode();
    }

    /**
     * Write this object to an output.
     *
     * @param output To write to.
     * @throws IOException If that fails.
     */
    public void serialize(final DataCellDataOutput output) throws IOException {
        output.writeInt(size());
        for (DataCell c : m_cellList) {
            output.writeDataCell(c);
        }
    }

    /**
     * Static deserializer for a list.
     *
     * @param input To read from.
     * @return A newly create list.
     * @throws IOException If that fails
     * @see DataCellSerializer#deserialize(DataCellDataInput)
     */
    public static BlobSupportDataCellList deserialize(
            final DataCellDataInput input) throws IOException {
        int size = input.readInt();
        if (size < 0) {
            throw new IOException("Invalid size: " + size);
        }
        ArrayList<DataCell> cells = new ArrayList<DataCell>(size);
        for (int i = 0; i < size; i++) {
            cells.add(input.readDataCell());
        }
        return new BlobSupportDataCellList(cells);
    }

    /**
     * Factory method to create a list based on a collection.
     * <p>
     * If the underlying collection stems from a {@link DataRow} (as read from a
     * any table), consider to use {@link #create(DataRow, int[])} in order to
     * minimize cell access.
     *
     * @param coll The underlying collection.
     * @return The newly created list.
     * @throws NullPointerException If the argument is null or contains null
     *             values.
     */
    public static BlobSupportDataCellList create(final Collection<DataCell> coll) {
        return new BlobSupportDataCellList(coll);
    }

    /**
     * Create new list based on selected cell from a {@link DataRow}. Using
     * this method will check if the row is returned by a
     * {@link BufferedDataTable} and will handle blobs appropriately.
     *
     * @param row The underlying row
     * @param cols The indices of interest.
     * @return A newly create list.
     * @throws NullPointerException If either argument is null.
     * @throws IndexOutOfBoundsException If the indices are invalid.
     */
    public static BlobSupportDataCellList create(final DataRow row,
            final int[] cols) {
        ArrayList<DataCell> coll = new ArrayList<DataCell>(cols.length);
        for (int i = 0; i < cols.length; i++) {
            DataCell c;
            if (row instanceof BlobSupportDataRow) {
                c = ((BlobSupportDataRow)row).getRawCell(cols[i]);
            } else {
                c = row.getCell(cols[i]);
            }
            coll.add(c);
        }
        return create(coll);
    }
}
