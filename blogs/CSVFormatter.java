package blogs;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CSV formatter to format the LogRecord into table view where the columns are :
 * [Date] [Time] [User] [Operation] [additional attributes]
 * CSV delimiter is a char ';'
 */
public class CSVFormatter extends BFormatter {	
	private static final String MESSAGE_FORMAT = "{0,date,short};{0,time,HH:mm:ss.SSS};{1};{2};{3};{4}";
	private static final String LINE_SEP = "\r\n";
	
	/**
	 * Format a record according to the MESSAGE_FORMAT
	 */
	@Override
	public String format(BLogRecord record) {
		String message = MessageFormat.format(MESSAGE_FORMAT + LINE_SEP,
												record.getMillis(),
												record.getUser(),
												record.getMessage(),
												record.getOperation(),
												record.getMessageType());
		return message;
	}
	
	/**
	 * Get string of current date. The format of date is "yyyy-MM-dd" 
	 * @return
	 */
	public String getDateString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(new Date());
	}

}
