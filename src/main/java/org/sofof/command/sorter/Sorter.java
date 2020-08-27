/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.sorter;

import org.sofof.SofofException;
import org.sofof.command.Select;
import java.io.Serializable;
import java.util.List;

/**
 * Sort objects in the result of a query
 *
 * @see Select
 * @author Rami Manaf Abdullah
 */
public interface Sorter extends Serializable {

    static final long serialVersionUID = 766545;

    /**
     * sorts the list objects
     *
     * @param list
     * @throws SofofException
     */
    public void sort(List list) throws SofofException;

    /**
     * Sort objects by this sorter after sorting them with the passed sorter.
     * The priority of sorting will be to this sorter then to the passed one
     *
     * @param sorter sorter with less priority
     * @return new sorter
     */
    default public Sorter after(Sorter sorter) {
        return new SortAfter(sorter, this);
    }

}

class SortAfter implements Sorter, Serializable {

    private Sorter first;
    private Sorter after;

    SortAfter(Sorter first, Sorter after) {
        this.first = first;
        this.after = after;
    }

    @Override
    public void sort(List list) throws SofofException {
        first.sort(list);
        after.sort(list);
    }

}
