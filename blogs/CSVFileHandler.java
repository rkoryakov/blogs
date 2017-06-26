package blogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple CSV file logging {@code CSVFileHandler}.
 * <p>
 * The <tt>CSVFileHandler</tt> create a set of files based on the given file name 
 * pattern. If a file size - <tt>limit</tt> has been reached then a new file with
 * generation number at the end of the file name will be created. Because {@code CSVFileHandler} 
 * is intended to be used with {@code BLogManager} that's using concurrent records pool 
 * to send the records to a single {@code BHandler}.
 * <p>
 * The pattern consists of a string that includes the following special
 * components that will be replaced at runtime:
 * <ul>
 * <li>     "/"    the local pathname separator </li>
 * <li>     "%d"   the base directory of business logs (defined in cfg.properties, see {@link BLogManager})</li>
 * <li>     "%t"   the current date "yyyy-MM-dd"</li>
 * <li>     "%i"   the generation number to distinguish logs (will be incremented when the 
 * 					current file size is reached the limit)</li>
 * </ul>
 * <p>
 * Note<br>
 * When you use special components within a pattern, be careful. For example: pattern "%t" will
 * try to open/create the file with a name that contains current date! In this case the file 
 * created yesterday will differ from the file you'll create tomorrow. 
 */
public class CSVFileHandler extends BStreamHandler {
	private MeteredStream meter;
	private int limit; 
	private String pattern;

	public static final int DEFAULT_FILE_SIZE = 30000000; // 30 Mb
	public static final String DEFAULT_CSV_FILE_NAME_PATTREN = "./" + BSolution.ALL.getPath() + "_%t%i.csv";
	public static final String DEFAULT_ENCODING = "utf-8";
	
	// A metered stream is a subclass of OutputStream that
	// (a) forwards all its output to a target stream
	// (b) keeps track of how many bytes have been written
	private class MeteredStream extends OutputStream {
		OutputStream out;
		int written;

		MeteredStream(OutputStream out, int written) {
			this.out = out;
			this.written = written;
		}

		public void write(int b) throws IOException {
			out.write(b);
			written++;
		}

		public void write(byte buff[]) throws IOException {
			out.write(buff);
			written += buff.length;
		}

		public void write(byte buff[], int off, int len) throws IOException {
			out.write(buff, off, len);
			written += len;
		}

		public void flush() throws IOException {
			out.flush();
		}

		public void close() throws IOException {
			out.close();
		}
	}

	private void open(String pattern) throws IOException {
		int len = 0;
		int g = 0;
		File fname = null;
		
		while (true) {
			g ++;
			fname = generateFileName(pattern, g);
			if (fname.length() < this.limit) {
				break;
			}
		}
		len = (int) fname.length();
		FileOutputStream fout = new FileOutputStream(fname.toString(), true);
		//BufferedOutputStream bout = new BufferedOutputStream(fout);
		meter = new MeteredStream(fout, len);
		setOutputStream(meter);
	}

	/**
	 * Generate file name from the given pattern.
	 * <p>It's improved version of FileHandler's generate() method.
	 * <p>
	 * @param pattern file pattern
	 * @param number file number
	 * @return
	 */
	private File generateFileName(String pattern, int number) {
		super.close();
		BLogManager manager = BLogManager.getLogManager();
		StringBuilder pathPart = new StringBuilder("");
		File fname = null;
		
		// iterate over the pattern
		for (int i = 0; i < pattern.length(); i ++) {
			char ch = pattern.charAt(i);
			if (ch == File.separatorChar) {
				// we reached a path separator
				if (fname != null) {
					fname = new File(fname, pathPart.toString());
				} else {
					fname = new File(pathPart.toString());
				}
				pathPart.replace(0, pathPart.length(), "");
			} else
			if (ch == '%' && i < pattern.length() - 1) {
				char ch2 = pattern.charAt(i + 1);
				switch(ch2) {
				case 'd':
					String dir = manager.getProperty(BLogManager.BLOGS_DIR_PROP, manager.getDefaultBaseDir());
					fname = new File(dir);
					pathPart.replace(0, pathPart.length(), "");
					i ++;
					break;
				case 't':
					CSVFormatter formatter = (CSVFormatter)getFormatter();
					String date = formatter.getDateString();
					pathPart.append(date);
					i ++;
					break;
				case 'i':
					pathPart.append("_part").append(number);
					i ++;
					break;
				default:
					i ++;
				}
			} else {
				pathPart.append(ch);
			}
		}
		
		if (pathPart.length() > 0) {
			fname = new File(fname, pathPart.toString());
		}
		
		return fname;
	}
	
	/**
	 * Configure a CSVFileHandler from BLogManager
	 * properties and/or default values
	 */
	private void configure() {
		BLogManager manager = BLogManager.getLogManager();
		setFormatter(new CSVFormatter());
		limit = manager.getProperty(BLogManager.CSV_MAX_FILE_SIZE_PROP, DEFAULT_FILE_SIZE);
		pattern = manager.getProperty(BLogManager.CSV_FILE_NAME_PATTREN_PROP, DEFAULT_CSV_FILE_NAME_PATTREN);
		try {
			setEncoding(manager.getProperty(BLogManager.CSV_ENCODING_PROP, DEFAULT_ENCODING));
		} catch (Exception ex2) {
			// doing a setEncoding with null should always work.
			// assert false;
		}
	}

	/**
	 * Construct a default <tt>CSVFileHandler</tt>. This will be configured
	 * entirely from <tt>BLogManager</tt> properties (or their default values).
	 * 
	 * @exception IOException
	 *                if there are IO problems opening the files.
	 */
	public CSVFileHandler() throws IOException {
		configure();
		open(pattern);
	}

	/**
	 * Initialize a <tt>CSVFileHandler</tt> to write to the given filename.When
	 * (approximately) the default DEFAULT_FILE_SIZE limit has been written to one file, 
	 * another file will be opened.
	 * <p>
	 * The <tt>CSVFileHandler</tt> is configured based on <tt>BLogManager</tt>
	 * properties (or their default values) except that the given pattern
	 * argument is used as the filename pattern, the file limit is set to no
	 * limit, and the file count is set to one.
	 * <p>
	 * There is no limit on the amount of data that may be written, so use this
	 * with care.
	 * 
	 * @param pattern
	 *            the name of the output file
	 * @exception IOException
	 *                if there are IO problems opening the files.
	 * @exception IllegalArgumentException
	 *                if pattern is an empty string
	 */
	public CSVFileHandler(String pattern) throws IOException {
		if (pattern == null || pattern.length() < 1) {
			throw new IllegalArgumentException();
		}

		configure();
		this.pattern = pattern;
		open(pattern);
	}

	/**
	 * Initialize a <tt>CSVFileHandler</tt> to write to a set of files. When
	 * (approximately) the given limit has been written to one file, another
	 * file will be opened.
	 * <p>
	 * The <tt>CSVFileHandler</tt> is configured based on <tt>BLogManager</tt>
	 * properties (or their default values) except that the given pattern
	 * argument is used as the filename pattern, the file limit is set to the
	 * limit argument.
	 * 
	 * @param pattern
	 *            the pattern for naming the output file
	 * @param limit
	 *            the maximum number of bytes to write to any file
	 * @exception IOException
	 *                if there are IO problems opening the files.
	 * @exception IllegalArgumentException
	 *                if limit < 0, or count < 1.
	 * @exception IllegalArgumentException
	 *                if pattern is an empty string
	 */
	public CSVFileHandler(String pattern, int limit) throws IOException {
		if (limit < 0 || pattern == null || pattern.length() < 1) {
			throw new IllegalArgumentException();
		}
		configure();
		this.pattern = pattern;
		this.limit = limit;
		open(pattern);
	}

	/**
	 * Initialize a <tt>CSVFileHandler</tt> to write to a set of files. When
	 * (approximately) the given limit has been written to one file, another
	 * file will be opened.
	 * <p>
	 * The <tt>CSVFileHandler</tt> is configured based on <tt>BLogManager</tt>
	 * properties (or their default values) except that the given pattern
	 * argument is used as the filename pattern, the file limit is set to the
	 * limit argument.
	 * 
	 * @param pattern
	 *            the pattern for naming the output file
	 * @param limit
	 *            the maximum number of bytes to write to any file
	 * @param formatter
	 * 			  the formatter 
	 * @exception IOException
	 *                if there are IO problems opening the files.
	 * @exception IllegalArgumentException
	 *                if limit < 0, or count < 1.
	 * @exception IllegalArgumentException
	 *                if pattern is an empty string
	 */
	public CSVFileHandler(String pattern, int limit, BFormatter formatter) throws IOException {
		if (limit < 0 || pattern == null || pattern.length() < 1) {
			throw new IllegalArgumentException();
		}
		configure();
		this.pattern = pattern;
		this.limit = limit;
		setFormatter(formatter);
		open(pattern);
	}
	
	/**
	 * Format and publish a <tt>LogRecord</tt>.
	 * 
	 * @param record
	 *            description of the log event. A null record is silently
	 *            ignored and is not published
	 */
	public synchronized void publish(BLogRecord record) {
		super.publish(record);
		flush();
		if (meter.written >= limit) {
			super.close();
			try {
				open(pattern);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Close all the files.
	 */
	public synchronized void close() {
		super.close();
	}
}
