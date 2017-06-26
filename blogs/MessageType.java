package blogs;

public enum MessageType {
	INFO(0),
	WARNING(1),
	ERROR(2);
	
	int type;
	
	MessageType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return String.valueOf(this.type);
	}
}
