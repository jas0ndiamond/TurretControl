package org.jason.turretcontrol.magazine;

public abstract class Magazine 
{
	public final static int DEFAULT_ROUNDS_REMAINING = 30;

	public static final int DEFAULT_CAPACITY = 0;

	public static final String DEFAULT_DESC = "Generic Magazine";
	
	public int capacity;
	
	public int roundsRemaining;
	
	public String name;
	
	protected Magazine(int roundsRemaining, int capacity, String name)
	{
		if (capacity <= 0)
		{
			
		}
		
		if(roundsRemaining < 0)
		{
			
		}
		
		this.roundsRemaining = roundsRemaining;
		this.capacity = capacity;
		this.name = name;
	}
	
	protected Magazine(int capacity)
	{
		this(DEFAULT_ROUNDS_REMAINING, capacity, DEFAULT_DESC);
	}
	
	protected Magazine(int roundsRemaining, int capacity)
	{
		this(roundsRemaining, capacity, DEFAULT_DESC);
	}
	
	public int getRoundsRemaining()
	{
		return roundsRemaining;
	}
	
	public int getCapacity()
	{
		return capacity;
	}
	
	public void getRound()
	{
		if(roundsRemaining > 0)
		{
			roundsRemaining--;
		}
	}
}
