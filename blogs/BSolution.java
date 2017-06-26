package blogs;

public enum BSolution {
	/**
	 * Business solution OFFERTS. 
	 */
	OFFERTS("OFFERTS", "OFFERTS"),
	/**
	 * Business solution NSU
	 */
	NSU("NSU", "NSU"),
	/**
	 * Business solution PKO
	 */
	PKO("PKO", "PKO"),
	/**
	 * Business solution MTR
	 */
	MTR("MTR", "MTR"),
	/**
	 * Business solution PIR/SMR
	 */
	PIRSMR("PIRSMR", "PIRSMR"),
	/**
	 * Business solution PSF
	 */
	PSF("PSF", "PSF"),
	/**
	 * Business solution ALL 
	 */
	ALL("ALL", "ALL"),
	/**
	 * Business solution ECK
	 */
	ECK("ECK", "ECK"),
	/**
	 * Business solution ESM
	 */
	ESM("ESM", "ESM"),
	/**
	 * Use it if you want to define your own solution. You will able to change 
	 * this empty solution using methods {@code setsetName(String name)} and {@code setPath(String path)}
	 */
	EMPTY("", "");
	
	
	private String name;
	private String path;
	BSolution(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
