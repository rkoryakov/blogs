package blogs;

public enum BOperation {
	UPLOAD_DOCUMENT("UD"),
	REMOVE_DOCUMENT("RD"),
	CREATE_REQUEST("CR"),
	SAVE_REQUEST("SR"),
	CHANGE_REQUEST("CHR"),
	REMOVE_REQUEST("RR"),
	EMPTY("");
	
	String operation;
	
	BOperation(String operation) {
		this.operation = operation;
	}
	
	public String toString() {
		return this.operation;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String getOperation() {
		return this.operation;
	}
}
