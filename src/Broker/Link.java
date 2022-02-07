package Broker;

public class Link {
	public String id;
	public long dataSize;
	
	public Link(String string, long l) {
		id = string;
		dataSize = l;
	}
	
	public long getDataSize() {
		return (dataSize) ;
	}
	
	public String getId() {
		return(id) ;
	}
}