package blogs;

import java.io.UnsupportedEncodingException;

/**
 * A <tt>BHandler</tt> object takes log messages from a
 * <tt>BLogger</tt> and exports them. It might for example, write them to
 * a console or write them to a file, or send them to a network logging service,
 * or forward them to an OS log, or whatever.
 */
public abstract class BHandler {

	//private Filter filter;
	private BFormatter formatter;
	private String encoding;

	protected BHandler() {
	}

	/**
	 * Publish a <tt>BusinesBLogRecord
	 * <p>
	 * The logging request was made initially to a <tt>BLogger</tt>
	 * object, which initialized the <tt>BusinessLogRecBLogRecordarded it
	 * here.
	 * <p>
	 * The <tt>BHandler</tt> is responsible for formatting the message,
	 * when and if necessary. The formatting should include localization.
	 * 
	 * @param record
	 *            description of the log event. A null record is silently
	 *            ignored and is not published
	 */
	public abstract void publish(BLogRecord record);
	
	/**
	 * Flush any buffered output.
	 */
	public abstract void flush();

	/**
	 * Close the <tt>BHandler</tt> and free all associated resources.
	 * <p>
	 * The close method will perform a <tt>flush</tt> and then close the
	 * <tt>BHandler</tt>. After close has been called this <tt>BHandler</tt>
	 * should no longer be used. Method calls may either be silently ignored or
	 * may throw runtime exceptions.
	 * 
	 */
	public abstract void close();

	/**
	 * Set a <tt>Formatter</tt>. This <tt>Formatter</tt> will be used to format
	 * <tt>BusinessLogRecBLogRecord <tt>BHandler</tt>.
	 * <p>
	 * Some <tt>Handlers</tt> may not use <tt>Formatters</tt>, in which case the
	 * <tt>Formatter</tt> will be remembered, but not used.
	 * <p>
	 * 
	 * @param newFormatter
	 *            the <tt>Formatter</tt> to use (may not be null)
	 */
	public void setFormatter(BFormatter newFormatter) {
		// Check for a null pointer:
		newFormatter.getClass();
		formatter = newFormatter;
	}

	/**
	 * Return the <tt>Formatter</tt> for this <tt>Handler</tt>.
	 * 
	 * @return the <tt>Formatter</tt> (may be null).
	 */
	public BFormatter getFormatter() {
		return formatter;
	}

	/**
	 * Set the character encoding used by this <tt>Handler</tt>.
	 * <p>
	 * The encoding should be set before any <tt>BusinessLogRecBLogRecordten to
	 * the <tt>Handler</tt>.
	 * 
	 * @param encoding
	 *            The name of a supported character encoding. May be null, to
	 *            indicate the default platform encoding.
	 * @exception UnsupportedEncodingException
	 *                if the named encoding is not supported.
	 */
	public void setEncoding(String encoding) throws java.io.UnsupportedEncodingException {
		if (encoding != null) {
			try {
				if (!java.nio.charset.Charset.isSupported(encoding)) {
					throw new UnsupportedEncodingException(encoding);
				}
			} catch (java.nio.charset.IllegalCharsetNameException e) {
				throw new UnsupportedEncodingException(encoding);
			}
		}
		this.encoding = encoding;
	}

	/**
	 * Return the character encoding for this <tt>Handler</tt>.
	 * 
	 * @return The encoding name. May be null, which indicates the default
	 *         encoding should be used.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Set a <tt>Filter</tt> to control output on this <tt>Handler</tt>.
	 * <P>
	 * For each call of <tt>publish</tt> the <tt>Handler</tt> will call this
	 * <tt>Filter</tt> (if it is non-null) to check if the <tt>LogRecord</tt>
	 * should be published or discarded.
	 * 
	 * @param newFilter
	 *            a <tt>Filter</tt> object (may be null)
	 */
//	public void setFilter(Filter newFilter) throws SecurityException {
//		filter = newFilter;
//	}

	/**
	 * Get the current <tt>Filter</tt> for this <tt>Handler</tt>.
	 * 
	 * @return a <tt>Filter</tt> object (may be null)
	 */
//	public Filter getFilter() {
//		return filter;
//	}
}
