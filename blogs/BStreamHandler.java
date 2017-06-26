package blogs;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class BStreamHandler extends BHandler {
	private OutputStream output;
	private Writer writer;

	// Private method to configure a StreamHandler from LogManager
	// properties and/or default values as specified in the class
	// javadoc.
	private void configure() {
	}

	/**
	 * Create a <tt>BStreamHandler</tt>, with no current output stream.
	 */
	public BStreamHandler() {
		configure();
	}

	/**
	 * Create a <tt>BStreamHandler</tt> with a given <tt>BFormatter</tt> and
	 * output stream.
	 * <p>
	 * 
	 * @param out
	 *            the target output stream
	 * @param formatter
	 *            Formatter to be used to format output
	 */
	public BStreamHandler(OutputStream out, BFormatter formatter) {
		configure();
		setFormatter(formatter);
		setOutputStream(out);
	}

	/**
	 * Change the output stream.
	 * <P>
	 * If there is a current output stream then the <tt>BFormatter</tt>'s tail
	 * string is written and the stream is flushed and closed. Then the output
	 * stream is replaced with the new output stream.
	 * 
	 * @param out
	 *            New output stream. May not be null.
	 * 
	 */
	protected synchronized void setOutputStream(OutputStream out) {
		if (out == null) {
			throw new NullPointerException();
		}
		flushAndClose();
		output = out;
		String encoding = getEncoding();
		if (encoding == null) {
			writer = new OutputStreamWriter(output);
		} else {
			try {
				writer = new OutputStreamWriter(output, encoding);
			} catch (UnsupportedEncodingException ex) {
				// This shouldn't happen. The setEncoding method
				// should have validated that the encoding is OK.
				throw new Error("Unexpected exception " + ex);
			}
		}
	}

	/**
	 * Set (or change) the character encoding used by this <tt>BHandler</tt>.
	 * <p>
	 * The encoding should be set before any <tt>BLogRecords</tt> are written to
	 * the <tt>BHandler</tt>.
	 * 
	 * @param encoding
	 *            The name of a supported character encoding. May be null, to
	 *            indicate the default platform encoding.
	 * 
	 * @exception UnsupportedEncodingException
	 *                if the named encoding is not supported.
	 */
	public void setEncoding(String encoding) throws java.io.UnsupportedEncodingException {
		super.setEncoding(encoding);
		if (output == null) {
			return;
		}
		// Replace the current writer with a writer for the new encoding.
		flush();
		if (encoding == null) {
			writer = new OutputStreamWriter(output);
		} else {
			writer = new OutputStreamWriter(output, encoding);
		}
	}

	/**
	 * Format and publish a <tt>BLogRecord</tt>.
	 * <p>
	 * The <tt>BStreamHandler</tt> first checks if there is an
	 * <tt>OutputStream</tt> If not it silently returns. If so, it calls its
	 * <tt>Formatter</tt> to format the record and then writes the result to the
	 * current output stream.
	 * <p>
	 * If this is the first <tt>BLogRecord</tt> to be written to a given
	 * <tt>OutputStream</tt>, the <tt>BFormatter</tt>'s "head" string is written
	 * to the stream before the <tt>BLogRecord</tt> is written.
	 * 
	 * @param record
	 *            description of the log event. A null record is silently
	 *            ignored and is not published
	 */
	public synchronized void publish(BLogRecord record) {

		String msg;
		try {
			msg = getFormatter().format(record);
		} catch (Exception ex) {
			// We don't want to throw an exception here, but we
			// report the exception to any registered ErrorManager.
			System.err.println(ex.getMessage());
			return;
		}

		try {
			writer.write(msg);
		} catch (Exception ex) {
			// We don't want to throw an exception here, but we
			// report the exception to any registered ErrorManager.
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Flush any buffered messages.
	 */
	public synchronized void flush() {
		if (writer != null) {
			try {
				writer.flush();
			} catch (Exception ex) {
				// We don't want to throw an exception here, but we
				// report the exception to any registered ErrorManager.
				System.err.println(ex.getMessage());
			}
		}
	}

	private synchronized void flushAndClose() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				// We don't want to throw an exception here, but we
				// report the exception to any registered ErrorManager.
				System.err.println(ex.getMessage());
			}
			writer = null;
			output = null;
		}
	}

	/**
	 * Close the current output stream.
	 */
	public synchronized void close() {
		flushAndClose();
	}
}
