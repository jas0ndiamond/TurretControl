package org.jason.turretcontrol.sensors;

import java.io.IOException;
import java.io.InputStream;

public abstract class TurretSystem 
{
	public static double getCPUTemp() throws InterruptedException, IOException
	{
		StringBuilder rawCpuTemp = new StringBuilder(getSystemCallOutput("/bin/cat /sys/class/thermal/thermal_zone0/temp"));
		
		//6-digits: probably 999 > x > 100C
		//5-digits: probably 100 > x >= 10
		//4-digits: probably 10 > x > 1
		//probably not going to be colder than 1C
		//probably not going to be hotter than 999C
	
		if(rawCpuTemp.length() == 6)
		{
			rawCpuTemp.insert(3, ".");
		}
		else if(rawCpuTemp.length() == 5)
		{
			rawCpuTemp.insert(2, ".");			
		}
		else if(rawCpuTemp.length() == 4)
		{
			rawCpuTemp.insert(1, ".");
		}
		
		return Double.parseDouble(rawCpuTemp.toString());
		
	}
	
	public static double getGPUTemp() throws InterruptedException, IOException
	{
		String output = getSystemCallOutput("/opt/vc/bin/vcgencmd measure_temp");
		return Double.parseDouble(output.replace("temp=", "").replace("'C", ""));
	}
	
	public static int[] getMemoryUtilization() throws InterruptedException, IOException
	{
		String output = getSystemCallOutput("/bin/cat /proc/meminfo");
		
		String[] fields = output.split("\\n");
		
		int totalMem = Integer.parseInt(fields[0].split("\\s+")[1]);
		int freeMem =  Integer.parseInt(fields[1].split("\\s+")[1]);
		
		int usedMem = totalMem - freeMem; 
		
		return new int[]
		{
			totalMem / 1024,
			usedMem / 1024,
			freeMem / 1024
		};
	}

	public static long[] getJVMMemoryUtilization()
	{
		Runtime rt = Runtime.getRuntime();
		
		long totalMem = rt.totalMemory();
		long freeMem = rt.freeMemory();
		
		return new long[]
		{
			totalMem / (1024 * 1024),
			(totalMem - freeMem)/ (1024 * 1024),
			freeMem/ (1024 * 1024)
		};
	}
	
	public static double[] getLoadAverage() throws InterruptedException, IOException
	{
		String output = getSystemCallOutput("/bin/cat /proc/loadavg");
		String[] fields = output.split("\\s+");
		
		return new double[]
		{
			Double.parseDouble(fields[0]),
			Double.parseDouble(fields[1]),
			Double.parseDouble(fields[2])
		};
	}
	
	private static String getSystemCallOutput(String syscall) throws InterruptedException, IOException
	{
	    Process p = null; 
	    InputStream is = null;
	    
	    StringBuilder output = new StringBuilder();
	    
	    try
	    {
	    	p = Runtime.getRuntime().exec(syscall);
	    	is = p.getInputStream();
	    	
	    	int c; 
	    	while( (c = is.read()) != -1)
	    	{
	    		output.append((char)c);
	    	}
	    	
	    	p.waitFor(); // Let the process finish.
	    }
	    catch(Exception e)
	    {
	    	throw e;
	    }
	    finally
	    {
	    	if(is != null)
	    	{
	    		is.close();
	    	}
	    	
	    	if(p.isAlive())
	    	{
	    		p.destroy();
	    		
	    		int maxWait = 1500;
	    		int waited = 0;
	    		
	    		while(p.isAlive() && waited < maxWait)
	    		{
	    			Thread.sleep(500);
	    			waited += 500;
	    		}
	    		
	    		if(p.isAlive())
	    		{
	    			p.destroyForcibly();
	    		}
	    	}
	    	
	    }
	    
	    //System.out.println("Syscall: " + syscall + " has output: " + output.toString().replaceAll("\\n$", ""));
		
	    return output.toString().replaceAll("\\n$", "");
	}
}
