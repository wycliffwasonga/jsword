package org.crosswire.jsword.passage;

import java.util.Iterator;

/**
 * A KeyList is a Key that can contain other Keys.
 * 
 * The interface is modelled on the java.util.Set interface customized because
 * KeyLists can only store other Keys and simplified by making add() and remove()
 * return void and not a boolean.
 * 
 * <p><table border='1' cellPadding='3' cellSpacing='0'>
 * <tr><td bgColor='white' class='TableRowColor'><font size='-7'>
 *
 * Distribution Licence:<br />
 * JSword is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License,
 * version 2 as published by the Free Software Foundation.<br />
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.<br />
 * The License is available on the internet
 * <a href='http://www.gnu.org/copyleft/gpl.html'>here</a>, or by writing to:
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA<br />
 * The copyright to this program is held by it's authors.
 * </font></td></tr></table>
 * @see gnu.gpl.Licence
 * @author Joe Walker [joe at eireneh dot com]
 * @version $Id$
 */
public interface KeyList extends Key
{
    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     * @return the number of elements in this set (its cardinality).
     */
    public int size();

    /**
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * @param key element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains(Key key);

    /**
     * @return an iterator over the elements in this set.
     */
    public Iterator iterator();

    /**
     * Adds the specified element to this set if it is not already present.
     * @param key element to be added to this set.
     * @throws NullPointerException if the specified element is null
     */
    public void add(Key key);

    /**
     * Removes the specified element from this set if it is present.
     * @param key object to be removed from this set, if present.
     * @throws NullPointerException if the specified element is null
     */
    public void remove(Key key);

    /**
     * Removes all of the elements from this set (optional operation).
     * This set will be empty after this call returns (unless it throws an
     * exception).
     */
    public void clear();

    /**
     * @param index
     * @return
     */
    public Key get(int index);

    /**
     * @param that
     * @return
     */
    public int indexOf(Key that);
}