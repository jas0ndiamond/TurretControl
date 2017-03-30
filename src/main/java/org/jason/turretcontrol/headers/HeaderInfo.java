package main.java.org.jason.turretcontrol.headers;

public abstract class HeaderInfo {
	public abstract class Names
	{
		public static final String RESULT = "result";
		public static final String MESSAGE = "message";
		public static final String SAFETY = "safety";
		public static final String AMMO_COUNT = "ammo_count";
		public static final String MAG_SIZE = "mag_size";
	}
	
	public abstract class Values
	{
		public static final String JAMMED = "JAMMED";
		public static final String CYCLED = "CYCLED";
		public static final String SUCCESS = "SUCCESS";
		public static final String FAILED = "FAILED";
		public static final String SAFETY_ON = "ON";
		public static final String SAFETY_OFF = "OFF";
	}
}
