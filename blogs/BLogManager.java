package blogs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Business Logs manager provides the access to the registered 
 * business-loggers. It also initialises configuration properties 
 * that can be taken using {@code getProperty} method.
 */
public class BLogManager {
	// The global BLogManager object
	protected static BLogManager manager = new BLogManager();
	// Table of known loggers.  Maps names to BLoggers.
	protected ConcurrentMap<String, WeakReference<BLogger>> loggers = new ConcurrentHashMap<String, WeakReference<BLogger>>();
	protected Properties props = new Properties();
	
	public static final String BLOGS_DIR_PROP = "blogs.dir"; /* /usr/sap/<SID>/J00/log/MTO */
	public static final String CSV_MAX_FILE_SIZE_PROP = "csvfilehandler.maxfilesize";
	public static final String CSV_FILE_NAME_PATTREN_PROP = "csvfilehandler.file_name_pattern";
	public static final String LIFETIME_PROP = "blogger.lifetime";
    public static final String FLUSHPERIOD_PROP = "blogger.flushperiod";
	public static final String MAX_POOL_SIZE_PROP = "blogger.pool.size";
    public static final String CSV_ENCODING_PROP = "csvfilehandler.encoding";
	public static final String DEFAULT_BLOGS_DIR = "/usr/sap/<SID>/J00/log/MTO";
	
    /**
     * This constructor adds shutdown hook thread to safety finish all the 
     * BLoger instances when JVM shuts down normally or abruptly.
     */
    protected BLogManager() {
    	Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (Entry<String, WeakReference<BLogger>> logger : loggers.entrySet()) {
					logger.getValue().get().close();
				}
			}
		});
	}
	
    /**
     * Initialise the manager lazily when it's required 
     */
	protected void initLogManager() {
		try {
			readConfiguration();
		} catch (IOException e) {
			System.err.println("Error on readConfiguration() " + getClass().getCanonicalName() + ": "  + e.getMessage());
		}
	}
	
	/**
     * Return the global BLogManager object.
     */
    public static BLogManager getLogManager() {
        if (manager != null) {
            manager.initLogManager();
        }
        return manager;
    }
    
    /**
     * Add a named logger.  This does nothing and returns false if a logger
     * with the same name is already registered.
     * <p>
     * The BLogger factory methods call this method to register each
     * newly created Logger.
     * <p>
     * The application should retain its own reference to the Logger 
     * object to avoid it being garbage collected.  The BLogManager
     * may only retain a weak reference.
     *
     * @param   logger the new logger.
     * @return  true if the argument logger was registered successfully,
     *          false if a logger of that name already exists.
     * @exception NullPointerException if the logger name is null.
     */
	public boolean addLogger(BLogger logger) {
		final String name = logger.getLoggerName();
		if (name == null) {
			throw new NullPointerException();
		}

		WeakReference<BLogger> ref = loggers.get(name);
		if (ref != null) {
			if (ref.get() == null) {
				// ConcurrentHashMap holds stale weak reference
				// to a logger which has been GC-ed.
				// Allow to register new one.
				loggers.remove(name);
			} else {
				// We already have a registered logger with the given name.
				return false;
			}
		}

		// We're adding a new logger.
		// Note that we are creating a weak reference here.
		WeakReference<BLogger> bl = loggers.putIfAbsent(name, new WeakReference<BLogger>(logger));
		return (bl == null) ? true : false;
	}
	
    /**
     * Package-level method.
     * Find or create a specified logger instance. If a logger has
     * already been created with the given name it is returned.
     * Otherwise a new logger instance is created and registered
     * in the BLogManager global namespace.
     * 
     * @param name
     * @return
     */
    BLogger demandLogger(String name, BSolution solution) {
    	BLogger result = getLogger(name);
    	if (result == null) {
    		result = new BLogger(name, solution, this);
    		addLogger(result);
    		result = getLogger(name);
    	}
    	
    	return result;
    }
    
    BLogger demandLogger(String name, BSolution solution, BHandler handler) {
    	BLogger result = getLogger(name);
    	if (result == null) {
    		result = new BLogger(name, solution, handler, this);
    		addLogger(result);
    		result = getLogger(name);
    	}
    	
    	return result;
    }
    
    /**
     * Method to find a named logger.
     * @param name name of the logger 
     * @return  matching logger or null if none is found
     */
    public BLogger getLogger(String name) {
    	WeakReference<BLogger> ref = loggers.get(name);
    	if (ref == null) {
    		return null;
        }
    	BLogger logger = ref.get();
    	if (logger == null) {
            // Map holds stale weak reference 
            // to a logger which has been GC-ed.
            loggers.remove(name);
        }
    	
    	return logger;
    }
    
    /**
     * Reinitialize the logging properties and reread the logging configuration
     * from the CONFIG_FILE
     * @throws IOException
     */
    public void readConfiguration() throws IOException {
    	String cfgPath = System.getProperty(BLOGS_DIR_PROP, getDefaultBaseDir() + File.separatorChar  + "cfg.properties");
    	File f = new File(cfgPath);
		cfgPath = f.getCanonicalPath();
		if (f.exists()) {
			InputStream in = new FileInputStream(cfgPath);
			BufferedInputStream bin = new BufferedInputStream(in);
			try {
				// Load the properties
				props.load(bin);
			} finally {
				if (bin != null) {
					bin.close();
				}
			}
		}
    }
    
    protected String getDefaultBaseDir() {
    	return DEFAULT_BLOGS_DIR.replaceFirst("<SID>", System.getProperty("SAPSYSTEMNAME", ""));
    }
    
    /**
     * Get the value of a logging property.
     * The method returns defaultValue if the property is not found.
     * @param name	property name
     * @param defaultValue	default value
     * @return property value
     */
    public String getProperty(String name, String defaultValue) {
    	return props.getProperty(name, defaultValue);
    }
    
    /**
     * Get the value of a logging property.
     * The method returns defaultValue if the property is not found.
     * @param name	property name
     * @param defaultValue	default value
     * @return property	value
     */
    public int getProperty(String name, int defaultValue) {
    	int result = defaultValue;
    	String sResult = props.getProperty(name);
    	if (sResult != null) {
    		result = Integer.valueOf(sResult);
    	} 
    	
    	return result;
    }
}
