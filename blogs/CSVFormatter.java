package blogs;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * CSV formatter to format the LogRecord into table view where the columns are :
 * [Date] [Time] [User] [Operation] [additional attributes]
 * CSV delimiter is a char ';'
 */
public class CSVFormatter extends BFormatter {	
	private static final String MESSAGE_FORMAT = "{0};{0};{1};{3};{4};{2}";
	private static final String LINE_SEP = "\r\n";
	private String timeZone = "GMT";
	
	/**
	 * Format a record according to the MESSAGE_FORMAT
	 */
	@Override
	public String format(BLogRecord record) {
		MessageFormat messageFormat = new MessageFormat(MESSAGE_FORMAT + LINE_SEP);
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		sdf1.setTimeZone(TimeZone.getTimeZone(timeZone));
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss.SSS z");
		sdf2.setTimeZone(TimeZone.getTimeZone(timeZone));
		messageFormat.setFormats(new Format[]{sdf1, sdf2});
		
		StringBuffer res = new StringBuffer();
		messageFormat.format(new Object[]{  record.getMillis(),
											record.getUser(),
											record.getMessage(),
											record.getOperation(),
											record.getMessageType()}, res, null);
		
		return res.toString();
	}
	
	/**
	 * Get string of current date. The format of date is "yyyy-MM-dd" 
	 * @return
	 */
	public String getDateString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(TimeZone.getTimeZone(timeZone));
		return df.format(new Date());
	}

	public void setTimeZone(String timezone) {
		this.timeZone = timezone;
	}
}
