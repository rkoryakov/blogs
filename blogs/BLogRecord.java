package blogs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class BLogRecord implements java.io.Serializable {

	/**
	 * Business solution
	 */
	private BSolution businessSolution;
	/**
	 * @serial Class that issued logging call
	 */
	private String sourceClassName;

	/**
	 * @serial Method that issued logging call
	 */
	private String sourceMethodName;

	/**
	 * @serial Non-localized raw message text
	 */
	private String message;

	/**
	 * @serial Event time in milliseconds since 1970
	 */
	private long millis;

	/**
	 * @serial The Throwable (if any) associated with log message
	 */
	private Throwable thrown;

	/**
	 * @serial Name of the source Logger.
	 */
	private String loggerName;

	/**
	 * @serial User name
	 */
	private String user;
	
	/**
	 * @serial business operation
	 */
	private BOperation operation;
	
	/**
	 * @serial message type
	 */
	private MessageType messageType;
	/**
	 * @serial Resource bundle name to localized log message.
	 */
	private String resourceBundleName;

	private transient boolean needToInferCaller;
	private transient Object parameters[];
	private transient ResourceBundle resourceBundle;

	/**
	 * Construct a BLogRecord with the given message.
	 * <p>
	 * The sequence property will be initialized with a new unique value. These
	 * sequence values are allocated in increasing order within a VM.
	 * <p>
	 * The millis property will be initialized to the current time.
	 * <p>
	 * The thread ID property will be initialized with a unique ID for the
	 * current thread.
	 * <p>
	 * All other properties will be initialized to "null".
	 * 
	 * @param msg
	 *            the raw non-localized logging message (may be null)
	 */
	public BLogRecord(String msg) {
		message = msg;
		millis = System.currentTimeMillis();
		needToInferCaller = true;
	}

	public BSolution getBusinessSolution() {
		return businessSolution;
	}
	
	public void setBusinessSolution(BSolution bSolution) {
		this.businessSolution = bSolution;
	}
	
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	/**
	 * Get the source Logger name's
	 * 
	 * @return source logger name (may be null)
	 */
	public String getLoggerName() {
		return loggerName;
	}

	/**
	 * Set the source Logger name.
	 * 
	 * @param name
	 *            the source logger name (may be null)
	 */
	public void setLoggerName(String name) {
		loggerName = name;
	}

	/**
	 * Get the localization resource bundle
	 * <p>
	 * This is the ResourceBundle that should be used to localize the message
	 * string before formatting it. The result may be null if the message is not
	 * localizable, or if no suitable ResourceBundle is available.
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Set the localization resource bundle.
	 * 
	 * @param bundle
	 *            localization bundle (may be null)
	 */
	public void setResourceBundle(ResourceBundle bundle) {
		resourceBundle = bundle;
	}

	/**
	 * Get the localization resource bundle name
	 * <p>
	 * This is the name for the ResourceBundle that should be used to localize
	 * the message string before formatting it. The result may be null if the
	 * message is not localizable.
	 */
	public String getResourceBundleName() {
		return resourceBundleName;
	}

	/**
	 * Set the localization resource bundle name.
	 * 
	 * @param name
	 *            localization bundle name (may be null)
	 */
	public void setResourceBundleName(String name) {
		resourceBundleName = name;
	}

	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public BOperation getOperation() {
		return operation;
	}

	public void setOperation(BOperation operation) {
		this.operation = operation;
	}
	/**
	 * Get the name of the class that (allegedly) issued the logging request.
	 * <p>
	 * Note that this sourceClassName is not verified and may be spoofed. This
	 * information may either have been provided as part of the logging call, or
	 * it may have been inferred automatically by the logging framework. In the
	 * latter case, the information may only be approximate and may in fact
	 * describe an earlier call on the stack frame.
	 * <p>
	 * May be null if no information could be obtained.
	 * 
	 * @return the source class name
	 */
	public String getSourceClassName() {
		if (needToInferCaller) {
			inferCaller();
		}
		return sourceClassName;
	}

	/**
	 * Set the name of the class that (allegedly) issued the logging request.
	 * 
	 * @param sourceClassName
	 *            the source class name (may be null)
	 */
	public void setSourceClassName(String sourceClassName) {
		this.sourceClassName = sourceClassName;
		needToInferCaller = false;
	}

	/**
	 * Get the name of the method that (allegedly) issued the logging request.
	 * <p>
	 * Note that this sourceMethodName is not verified and may be spoofed. This
	 * information may either have been provided as part of the logging call, or
	 * it may have been inferred automatically by the logging framework. In the
	 * latter case, the information may only be approximate and may in fact
	 * describe an earlier call on the stack frame.
	 * <p>
	 * May be null if no information could be obtained.
	 * 
	 * @return the source method name
	 */
	public String getSourceMethodName() {
		if (needToInferCaller) {
			inferCaller();
		}
		return sourceMethodName;
	}

	/**
	 * Set the name of the method that (allegedly) issued the logging request.
	 * 
	 * @param sourceMethodName
	 *            the source method name (may be null)
	 */
	public void setSourceMethodName(String sourceMethodName) {
		this.sourceMethodName = sourceMethodName;
		needToInferCaller = false;
	}

	/**
	 * Get the "raw" log message, before localization or formatting.
	 * <p>
	 * May be null, which is equivalent to the empty string "".
	 * <p>
	 * This message may be either the final text or a localization key.
	 * <p>
	 * During formatting, if the source logger has a localization ResourceBundle
	 * and if that ResourceBundle has an entry for this message string, then the
	 * message string is replaced with the localized value.
	 * 
	 * @return the raw message string
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the "raw" log message, before localization or formatting.
	 * 
	 * @param message
	 *            the raw message string (may be null)
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Get the parameters to the log message.
	 * 
	 * @return the log message parameters. May be null if there are no
	 *         parameters.
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * Set the parameters to the log message.
	 * 
	 * @param parameters
	 *            the log message parameters. (may be null)
	 */
	public void setParameters(Object parameters[]) {
		this.parameters = parameters;
	}


	/**
	 * Get event time in milliseconds since 1970.
	 * 
	 * @return event time in millis since 1970
	 */
	public long getMillis() {
		return millis;
	}

	/**
	 * Set event time.
	 * 
	 * @param millis
	 *            event time in millis since 1970
	 */
	public void setMillis(long millis) {
		this.millis = millis;
	}

	/**
	 * Get any throwable associated with the log record.
	 * <p>
	 * If the event involved an exception, this will be the exception object.
	 * Otherwise null.
	 * 
	 * @return a throwable
	 */
	public Throwable getThrown() {
		return thrown;
	}

	/**
	 * Set a throwable associated with the log event.
	 * 
	 * @param thrown
	 *            a throwable (may be null)
	 */
	public void setThrown(Throwable thrown) {
		this.thrown = thrown;
	}

	private static final long serialVersionUID = 5372048053134512534L;

	/**
	 * @serialData Default fields, followed by a two byte version number (major
	 *             byte, followed by minor byte), followed by information on the
	 *             log record parameter array. If there is no parameter array,
	 *             then -1 is written. If there is a parameter array (possible
	 *             of zero length) then the array length is written as an
	 *             integer, followed by String values for each parameter. If a
	 *             parameter is null, then a null String is written. Otherwise
	 *             the output of Object.toString() is written.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// We have to call defaultWriteObject first.
		out.defaultWriteObject();

		// Write our version number.
		out.writeByte(1);
		out.writeByte(0);
		if (parameters == null) {
			out.writeInt(-1);
			return;
		}
		out.writeInt(parameters.length);
		// Write string values for the parameters.
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] == null) {
				out.writeObject(null);
			} else {
				out.writeObject(parameters[i].toString());
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// We have to call defaultReadObject first.
		in.defaultReadObject();

		// Read version number.
		byte major = in.readByte();
		byte minor = in.readByte();
		if (major != 1) {
			throw new IOException("BLogRecord: bad version: " + major + "."
					+ minor);
		}
		int len = in.readInt();
		if (len == -1) {
			parameters = null;
		} else {
			parameters = new Object[len];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = in.readObject();
			}
		}
		// If necessary, try to regenerate the resource bundle.
		if (resourceBundleName != null) {
			try {
				resourceBundle = ResourceBundle.getBundle(resourceBundleName);
			} catch (MissingResourceException ex) {
				// This is not a good place to throw an exception,
				// so we simply leave the resourceBundle null.
				resourceBundle = null;
			}
		}

		needToInferCaller = false;
	}

	// Private method to infer the caller's class and method names
	private void inferCaller() {
		needToInferCaller = false;
		// Get the stack trace.
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		// First, search back to a method in the Logger class.
		int ix = 0;
		while (ix < stack.length) {
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if (cname.equals("blogs.BLogger")) {
				break;
			}
			ix++;
		}
		// Now search for the first frame before the "Logger" class.
		while (ix < stack.length) {
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if (!cname.equals("blogs.BLogger")) {
				// We've found the relevant frame.
				setSourceClassName(cname);
				setSourceMethodName(frame.getMethodName());
				return;
			}
			ix++;
		}
		// We haven't found a suitable frame, so just punt. This is
		// OK as we are only committed to making a "best effort" here.
	}
}
