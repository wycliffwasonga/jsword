
package org.crosswire.jsword.book.basic;

import org.crosswire.common.util.I18NBase;

/**
 * Compile safe I18N resource settings.
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
 * @see docs.Licence
 * @author Joe Walker [joe at eireneh dot com]
 * @version $Id$
 */
public class I18N extends I18NBase
{
    public static final I18N DRIVER_READONLY = new I18N("This Book is read-only.");
    public static final I18N DELETE_NOTIMPL = new I18N("Sorry delete is not implemented yet.\n Some Bible names are simply the names of the directories in which they live.\n So you can manually delete the directory \"JSWORD/versions/{0}\"");
    public static final I18N NO_VERSE = new I18N("Invalid reference.");

    /** Initialise any resource bundles */
    static
    {
        init(I18N.class.getName());
    }

    /** Passthrough ctor */
    private I18N(String name)
    {
        super(name);
    }
}
