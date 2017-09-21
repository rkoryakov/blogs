package blogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * <tt>BLogger</tt> class is useful to log business messages in efficient way into 
 * convenient format. 
 * <p>
 * Every instance of <tt>BLogger</tt> class has its own local cache 
 * to hold <tt>BLogRecord</tt>s and writes these records down into its handlers periodically. Only one
 * instance of {@code BLogger} <strong>with a specified</strong> {@code loggerName} can be created.
 * Thus only one thread can publish this pool records. But several threads are able to invoke 
 * {@code getLogger()} method simultaneously. If there are no records in the 
 * cache during a specified time-period {@code DEFAULT_LIFETIME}, the logger will be closed.
 * <p>
 * A Periodic of flushing the records pool is defined by {@code flushPeriod}.
 * <p>
 * The messages have a corresponding format <tt>BFormatter</tt>.
 */
public class BLogger {
	// we hold the records in a memory cache 
	protected ConcurrentLinkedQueue<BLogRecord> recordPool = new ConcurrentLinkedQueue<BLogRecord>();
	// max amount of the records in the pool
	protected int maxPoolSize;
	protected int DEFAULT_MAX_POOL_SIZE = 5000; // 5000 records
	
	protected Thread flushThread = new Thread(new FlushBLogRecords());
	//flush period
	protected int flushPeriod; 
	// default flush period in milliseconds
	protected static final int DEFAULT_FLUSHPERIOD = 5000; // 5 seconds
	// logger lifetime
	protected int lifetime;
	// default logger lifetime during which there are no records in the pool (milliseconds)
	protected static final int DEFAULT_LIFETIME = 1000 * 10 * 60; // 10 minutes
	// a time when this flushThread has been started
	protected long startTime;
	protected List<BHandler> handlers;
	protected static final BHandler emptyHandlers[] = new BHandler[0];
	protected String loggerName;
	// Every thread may has its own user
	protected ThreadLocal<String> localUser = new ThreadLocal<String>();
	protected BSolution businessSolution;
	
	/**
	 * Get the logger from {@code BLogManeger} map. If there is no logger with a given {@code solution}
	 * in the {@code BLogManeger} map, the new instance of {@code BLogger} with the default
	 * {@code CSVFileHandler} will be created.
	 * @param user current user name
	 * @param solution specified business solution
	 * @return
	 */
	public synchronized static BLogger getLogger(String user, BSolution solution) {
		BLogManager manager = BLogManager.getLogManager();
		BLogger logger = manager.demandLogger(solution.getName(), solution);
		logger.setLocalUser(user);	
		return logger;
	}
	
	/**
	 * Get the logger from {@code BLogManeger} map. If there is no logger with a given {@code solution}
	 * in the {@code BLogManeger} map, the new instance of {@code BLogger} with a given 
	 * {@code handler} will be created.
	 * Note, If this logger is existing in the BLogManager then the given {@code handler}
	 * won't be added
	 * @param user current user name
	 * @param solution specified business solution
	 * @param handler specified handler
	 * @return
	 */
	public synchronized static BLogger getLogger(String user, BSolution solution, BHandler handler) {
		BLogManager manager = BLogManager.getLogManager();
		BLogger logger = manager.demandLogger(solution.getName(), solution, handler);
		logger.setLocalUser(user);
		return logger;
	}
	
	/**
	 * Get the logger from {@code BLogManeger} map and set a specified {@code lifetime}. 
	 * If there is no logger with given {@code solution} in the {@code BLogManeger} map, 
	 * the new instance of {@code BLogger} will be created.
	 * Note, If this logger is existing in the BLogManager then the given {@code handler}
	 * won't be added
	 * @param user
	 * @param solution
	 * @param handler
	 * @param lifetime
	 * @return
	 */
	public synchronized static BLogger getLogger(String user, BSolution solution, BHandler handler, int lifetime) {
		BLogger logger = getLogger(user, solution, handler);
		logger.lifetime = lifetime;
		return logger;
	}
	
	protected void configure(BLogManager manager) {
		this.lifetime = manager.getProperty(BLogManager.LIFETIME_PROP, DEFAULT_LIFETIME);
		this.flushPeriod = manager.getProperty(BLogManager.FLUSHPERIOD_PROP, DEFAULT_FLUSHPERIOD);
		this.maxPoolSize = manager.getProperty(BLogManager.MAX_POOL_SIZE_PROP, DEFAULT_MAX_POOL_SIZE);
	}
	
	protected BLogger(String name, BSolution solution, BLogManager manager) {
		this.loggerName = name;
		this.businessSolution = solution;
		configure(manager);
		CSVFileHandler handler = null;
		try {
			handler = new CSVFileHandler("%d/" + businessSolution.getPath() + "/" + businessSolution.getName() + "_%t%i.csv");
		} catch (IOException e) {
			System.err.println("Error on creating an instance of CSVFileHandler. " + e.getMessage());
			return;
		}
		addHandler(handler);
		// start flushing this records pool
		this.startTime = System.currentTimeMillis();
		startFlushing();
	}
	
	protected BLogger(String name, BSolution solution, BHandler handler, BLogManager manager) {
		this.loggerName = name;
		this.businessSolution = solution;
		configure(manager);
		addHandler(handler);
		// start flushing this records pool
		this.startTime = System.currentTimeMillis();
		startFlushing();
	}
	
	/**
	 * Flush the local cache {@code recordPool} periodically. If the records pool is empty
	 * during {@code lifetime} then we'll close the BLoger instance.
	 */ 
	private final class FlushBLogRecords implements Runnable {
		@Override
		public void run() {
			boolean finished = false;
			try {
				boolean isFlushed = true;
				long timeWhenNotFlushed = 0;
				while (true) {
					isFlushed = dequeueAllRecords();
					
					if (!isFlushed) {
						if (timeWhenNotFlushed == 0) {
							// remember the current time when there weren't records in the pool
							timeWhenNotFlushed = System.currentTimeMillis();
						}
						// logger had expired by lapse of time
						if (System.currentTimeMillis() - timeWhenNotFlushed > lifetime) {
							finished = true;
							break;
						}
					} else {
						// refresh the time if we found records during lifetime
						timeWhenNotFlushed = 0;
					}
					
					Thread.sleep(flushPeriod);
				}
			} catch(InterruptedException e) {
				System.err.println("The flushing thread '" + loggerName + "' was forcibly finished " 
						+ this.getClass().getCanonicalName());
			} finally {
				//close all the handlers
				// if this thread is completing by itself (lifetime)
				if (finished) {
					shutDown();
				} else {
					// otherwise, it has been closed by someone else (somebody invoked a close() method)
				}
			}
		}
	}
	
	/**
	 * Start thread that will flush the pool of records {@code recordPool} periodically
	 */
	private void startFlushing() {
		this.flushThread.start();
	}
	
	/**
	 * Force to stop flushing the pool of records {@code recordPool}
	 * This method will be invoked by ShutdownHook thread ( see {@link BLogManager} constructor)
	 */
	protected synchronized void close() {
		// stop flushing thread if it's running
		try {
			if (flushThread.isAlive()) {
				flushThread.interrupt();
				flushThread.join(40000);
			}
		} catch (InterruptedException ie) {
			System.err.println("Error on close() " + getClass().getCanonicalName() + " " + ie.getMessage());
		} finally {
			if (!flushThread.isAlive()) {
				shutDown();
			} else {
				System.err.println("Interrupted thread is alive " + getClass().getCanonicalName());
			}
		}
	}
	
	/**
	 * Shut down this logger and remove it from the {@link BLogManager}.
	 * It also closes and removes all the associated handlers.  
	 */
	private synchronized void shutDown() {
		// flush all the records in the pool
		dequeueAllRecords();
		// close & remove all the handlers
		for (BHandler h : getHandlers()) {
			h.close();
			removeHandler(h);
		}
		BLogManager.getLogManager().loggers.remove(this.getLoggerName());
		System.err.println("BLogger " + this.loggerName + " has been closed");
	}
	
	/**
	 * Write the message down into business logs with a default messageType {@code MessageType.INFO}
	 * and business operation {@code BOperation.EMPTY}. 
	 * If you want to write the message with your own business operation which is absent 
	 * in the {@code BOperation} you need to use {@code BOperation.EMPTY} and then set 
	 * your operation using {@code setOperation} method .
	 * @param message 
	 * 				message text
	 */
	public void log(String message) {
		log(message, BOperation.EMPTY);
	}
	
	/**
	 * Write the message down into business logs with a default {@code MessageType.INFO}. 
	 * If you want to write the message with your own business operation which is absent 
	 * in the {@code BOperation} you need to use {@code BOperation.EMPTY} and then set 
	 * your operation using {@code setOperation} method  method of {@code BOperation} enum.
	 * 
	 * @param message message text
	 * @param operation business operation
	 */
	public void log(String message, BOperation operation) {
		log(message, MessageType.INFO, operation);
	}
	
	/**
	 * Write the message down into business logs with a default {@code BOperation.EMPTY}. 
	 * If you want to write the message with your own business operation which is absent 
	 * in the {@code BOperation} you need to use {@code BOperation.EMPTY} and then set 
	 * your operation using {@code setOperation} method of {@code BOperation} enum.
	 * @deprecated use {@code log(message, messageType, operation)}
	 * @param message
	 * @param messageType
	 */
	public void log(String message, MessageType messageType) {
		log(message, messageType, BOperation.EMPTY);
	}
	
	/**
	 * Write the message down into business logs with a given {@code messageType} and {@code operation}. 
	 * If you want to write the message with your own business operation which is absent 
	 * in the {@code BOperation} you need to use {@code BOperation.EMPTY} and set 
	 * your operation using {@code setOperation} method of {@code BOperation} enum.
	 * @param message
	 * @param messageType
	 * @param operation
	 */
	public void log(String message, MessageType messageType, BOperation operation) {
		BLogRecord bLogRecord = createRecord(message);
		bLogRecord.setOperation(operation);
		bLogRecord.setMessageType(messageType);
		log(bLogRecord);
	} 
	
	protected BLogRecord createRecord(String message) {
		BLogRecord bLogRecord = new BLogRecord(message);
		bLogRecord.setUser(getLocalUser());
		bLogRecord.setLoggerName(getLoggerName());
		bLogRecord.setBusinessSolution(getBusinesSolution());
		bLogRecord.setOperation(BOperation.EMPTY);
		bLogRecord.setMessageType(MessageType.INFO);
		return bLogRecord;
	}
	
	/**
     * Log a BLogRecord.
     * <p>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
	protected void log(BLogRecord record) {
		this.recordPool.add(record);
		if (this.recordPool.size() > this.maxPoolSize) {
			flushRecord(record);
		}
	}
	
	/**
	 * Writes the record down into corresponding handlers 
	 * @param record
	 */
	protected void flushRecord(BLogRecord record) {
		BHandler targets[] = getHandlers();

		if (targets != null) {
			for (int i = 0; i < targets.length; i++) {
				targets[i].publish(record);
			}
		}
	}
	
	/**
	 * Dequeues {@code this.recordPool}.<p>(Gets and removes all the 
	 * records from the {@code this.recordPool} to write them down into 
	 * corresponding handlers, see {@linkplain BLogger.flushRecord})
	 * 
	 * @return true if there were records in the pool 
	 */
	protected boolean dequeueAllRecords() {
		boolean result = false;
		while (!recordPool.isEmpty()) {
			BLogRecord record = recordPool.poll();
			if (record != null) {
				flushRecord(record);
			}
			result = true;
		}
		
		BHandler targets[] = getHandlers();
		if (targets != null) {
			for (int i = 0; i < targets.length; i++) {
				targets[i].flush();
			}
		}
		
		return result;
	}
	
	/**
     * Add a log Handler to receive logging messages.
     * @param	handler	a logging Handler
     */
	public synchronized void addHandler(BHandler handler) {
		// Check for null handler
		handler.getClass();
		if (handlers == null) {
			handlers = new ArrayList<BHandler>();
		}
		handlers.add(handler);
	}

    /**
     * Remove a log Handler.
     * <P>
     * Returns silently if the given Handler is not found or is null
     * @param	handler	a logging Handler
     */
	public synchronized void removeHandler(BHandler handler) {
		if (handler == null) {
			return;
		}
		if (handlers == null) {
			return;
		}
		handlers.remove(handler);
	}
    
	/**
     * Get the Handlers associated with this logger.
     * <p>
     * @return  an array of all registered Handlers
     */
	public synchronized BHandler[] getHandlers() {
		if (handlers == null) {
			return emptyHandlers;
		}
		BHandler result[] = new BHandler[handlers.size()];
		result = handlers.toArray(result);
		return result;
	}
	
	public String getLoggerName() {
		return this.loggerName;
	}
	
	public String getLocalUser() {
		return localUser.get();
	}
	
	public void setLocalUser(String user) {
		this.localUser.set(user);
	}
	
	public BSolution getBusinesSolution() {
		return this.businessSolution;
	}
}
