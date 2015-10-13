package com.ms.soundpack1;

public class Track {
	  public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	private int id;
	    private String source;
	    
	 
	    public Track(){}
	 
	    public Track(String source) {
	        super();
	        this.source = source;
	      
	    }
	 
	    //getters & setters
	 
	    @Override
	    public String toString() {
	        return "Track [id=" + id + ", source=" + source 
	                + "]";
	    }
}
