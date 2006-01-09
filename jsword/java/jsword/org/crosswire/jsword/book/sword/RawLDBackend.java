/**
 * Distribution License:
 * JSword is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, version 2.1 as published by
 * the Free Software Foundation. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The License is available on the internet at:
 *       http://www.gnu.org/copyleft/lgpl.html
 * or by writing to:
 *      Free Software Foundation, Inc.
 *      59 Temple Place - Suite 330
 *      Boston, MA 02111-1307, USA
 *
 * Copyright: 2005
 *     The copyright to this program is held by it's authors.
 *
 * ID: $Id$
 */
package org.crosswire.jsword.book.sword;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.crosswire.common.activate.Activator;
import org.crosswire.common.activate.Lock;
import org.crosswire.common.util.ClassUtil;
import org.crosswire.common.util.FileUtil;
import org.crosswire.common.util.Logger;
import org.crosswire.common.util.Reporter;
import org.crosswire.common.util.StringUtil;
import org.crosswire.jsword.book.BookCategory;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.book.BookMetaData;
import org.crosswire.jsword.book.DataPolice;
import org.crosswire.jsword.passage.DefaultKeyList;
import org.crosswire.jsword.passage.DefaultLeafKeyList;
import org.crosswire.jsword.passage.Key;

/**
 * An implementation KeyBackend to read RAW format files.
 *
 * @see gnu.lgpl.License for license details.
 *      The copyright to this program is held by it's authors.
 * @author Joe Walker [joe at eireneh dot com]
 */
public class RawLDBackend extends AbstractBackend
{
    /**
     * Simple ctor
     * @param datasize We need to know how many bytes in the size portion of the index
     */
    public RawLDBackend(SwordBookMetaData sbmd, File rootPath, int datasize) throws BookException
    {
        super(sbmd, rootPath);
        this.datasize = datasize;

        if (datasize != 2 && datasize != 4)
        {
            throw new BookException(Msg.TYPE_UNKNOWN);
        }

        String dataPath = sbmd.getProperty(ConfigEntryType.DATA_PATH);
        File baseurl = new File(rootPath, dataPath);
        String path = baseurl.getAbsolutePath();

        idxFile = new File(path + SwordConstants.EXTENSION_INDEX);
        datFile = new File(path + SwordConstants.EXTENSION_DATA);

        if (!idxFile.canRead())
        {
            throw new BookException(Msg.READ_FAIL, new Object[] { idxFile.getAbsolutePath() });
        }

        if (!datFile.canRead())
        {
            throw new BookException(Msg.READ_FAIL, new Object[] { datFile.getAbsolutePath() });
        }
    }

    /* (non-Javadoc)
     * @see org.crosswire.common.activate.Activatable#activate(org.crosswire.common.activate.Lock)
     */
    public final void activate(Lock lock)
    {
        try
        {
            // Open the files
            idxRaf = new RandomAccessFile(idxFile, FileUtil.MODE_READ);
            datRaf = new RandomAccessFile(datFile, FileUtil.MODE_READ);
        }
        catch (IOException ex)
        {
            log.error("failed to open files", ex); //$NON-NLS-1$

            idxRaf = null;
            datRaf = null;
        }

        active = true;
    }

    /* (non-Javadoc)
     * @see org.crosswire.common.activate.Activatable#deactivate(org.crosswire.common.activate.Lock)
     */
    public final void deactivate(Lock lock)
    {
        try
        {
            idxRaf.close();
            datRaf.close();
        }
        catch (IOException ex)
        {
            log.error("failed to close files", ex); //$NON-NLS-1$
        }

        idxRaf = null;
        datRaf = null;

        active = false;
    }

    /* (non-Javadoc)
     * @see org.crosswire.jsword.book.sword.KeyBackend#readIndex()
     */
    public Key readIndex()
    {
        checkActive();

        BookMetaData bmd = getBookMetaData();
        Key reply = new DefaultKeyList(null, bmd.getName());

        boolean isDailyDevotional = bmd.getBookCategory().equals(BookCategory.DAILY_DEVOTIONS);
        // We use 1972 because it is a leap year.
        Calendar greg = new GregorianCalendar(1972, Calendar.JANUARY, 1);
        DateFormat nameDF = new SimpleDateFormat("d MMMM"); //$NON-NLS-1$

        int entrysize = OFFSETSIZE + datasize;
        long entries;
        try
        {
            entries = idxRaf.length() / entrysize;
        }
        catch (IOException ex)
        {
            Reporter.informUser(this, ex);
            return reply;
        }

        for (int entry = 0; entry < entries; entry++)
        {
            try
            {
                // Read the offset and size for this key from the index
                byte[] buffer = SwordUtil.readRAF(idxRaf, entry * entrysize, entrysize);
                int offset = SwordUtil.decodeLittleEndian32(buffer, 0);
                int size = -1;
                switch (datasize)
                {
                case 2:
                    size = SwordUtil.decodeLittleEndian16(buffer, 4);
                    break;
                case 4:
                    size = SwordUtil.decodeLittleEndian32(buffer, 4);
                    break;
                default:
                    assert false : datasize;
                }

                // Now read the data file for this key using the offset and size
                byte[] data = SwordUtil.readRAF(datRaf, offset, size);

                decipher(data);

                int keyend = SwordUtil.findByte(data, SEPARATOR);
                if (keyend == -1)
                {
                    DataPolice.report("Failed to find keyname. offset=" + offset + " data='" + new String(data) + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
                }

                byte[] keydata = new byte[keyend];
                System.arraycopy(data, 0, keydata, 0, keyend);

                String keytitle = new String(keydata).trim();
                // for some wierd reason plain text (i.e. SourceType=0) dicts
                // all get \ added to the ends of the index entries.
                if (keytitle.endsWith("\\")) //$NON-NLS-1$
                {
                    keytitle = keytitle.substring(0, keytitle.length() - 1);
                }

                if (isDailyDevotional)
                {
                    String[] parts = StringUtil.splitAll(keytitle, '.');
                    greg.set(Calendar.MONTH, Integer.parseInt(parts[0]) - 1);
                    greg.set(Calendar.DATE, Integer.parseInt(parts[1]));
                    keytitle = nameDF.format(greg.getTime());
                }

                Key key = new IndexKey(keytitle, offset, size, reply);

                reply.addAll(key);
            }
            catch (IOException ex)
            {
                log.error("Ignoring entry", ex); //$NON-NLS-1$
            }
            catch (NumberFormatException e)
            {
                log.error("Ignoring entry", e); //$NON-NLS-1$
            }
        }

        return reply;
    }

    /*
     * (non-Javadoc)
     * @see org.crosswire.jsword.book.sword.AbstractBackend#getRawText(org.crosswire.jsword.passage.Key, java.lang.String)
     */
    public String getRawText(Key key) throws BookException
    {
        checkActive();

        String charset = getBookMetaData().getBookCharset();

        if (!(key instanceof IndexKey))
        {
            throw new BookException(Msg.BAD_KEY, new Object[] { ClassUtil.getShortClassName(key.getClass()), key.getName() });
        }

        IndexKey ikey = (IndexKey) key;

        try
        {
            byte[] data = SwordUtil.readRAF(datRaf, ikey.offset, ikey.size);

            int keyend = SwordUtil.findByte(data, SEPARATOR);
            if (keyend == -1)
            {
                throw new BookException(Msg.READ_FAIL);
            }

            int remainder = data.length - (keyend + 1);
            byte[] reply = new byte[remainder];
            System.arraycopy(data, keyend + 1, reply, 0, remainder);

            return SwordUtil.decode(key, reply, charset);
        }
        catch (IOException ex)
        {
            throw new BookException(Msg.READ_FAIL, ex);
        }
    }

    /* (non-Javadoc)
     * @see org.crosswire.jsword.book.sword.AbstractBackend#isSupported()
     */
    public boolean isSupported()
    {
        return true;
    }

    /**
     * Helper method so we can quickly activate ourselves on access
     */
    protected final void checkActive()
    {
        if (!active)
        {
            Activator.activate(this);
        }
    }

    /**
     * Are we active
     */
    private boolean active;

    /**
     * Used to separate the key name from the key value
     */
    private static final byte SEPARATOR = 10; // ^M=CR=13=0x0d=\r ^J=LF=10=0x0a=\n

    /**
     * How many bytes in the offset pointers in the index
     */
    private static final int OFFSETSIZE = 4;

    /**
     * How many bytes in the size count in the index
     */
    private int datasize = -1;

    /**
     * The data random access file
     */
    private RandomAccessFile datRaf;

    /**
     * The index random access file
     */
    private RandomAccessFile idxRaf;

    /**
     * The data file
     */
    private File datFile;

    /**
     * The index file
     */
    private File idxFile;

    /**
     * The log stream
     */
    private static final Logger log = Logger.getLogger(RawLDBackend.class);

    /**
     * A Key that knows where the data is in the real file.
     */
    static class IndexKey extends DefaultLeafKeyList
    {
        /**
         * Setup with the key name and positions of data in the file
         */
        protected IndexKey(String text, int offset, int size, Key parent)
        {
            super(text, text, parent);

            this.offset = offset;
            this.size = size;
        }

        /**
         * Setup with the key name. Use solely for searching.
         */
        protected IndexKey(String text)
        {
            super(text, text, null);

            this.offset = -1;
            this.size = -1;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone()
        {
            return super.clone();
        }

        protected int offset;
        protected int size;
    }
}
