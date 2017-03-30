package test.java.turretcontrol.firecontrol;

import main.java.org.jason.turretcontrol.TurretControl;
import main.java.org.jason.turretcontrol.config.ConfigLoader;
import main.java.org.jason.turretcontrol.exception.JamOccurredException;
import main.java.org.jason.turretcontrol.exception.NoAmmoException;
import main.java.org.jason.turretcontrol.exception.SafetyEngagedException;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class G36GPIOFireControlTest 
{
	private TurretControl turretControl;

	@BeforeMethod
	public void setup() throws Exception
	{
		String tcConfig = ConfigLoader.getConfigJSONObject("src/test/java/G36TurretConfig.json")
				.getString("turret");
	
		try
		{	
			turretControl = new TurretControl(tcConfig);
		}
		catch(UnsatisfiedLinkError e)
		{
			throw new SkipException("Skipping gpio-dependent test suite. Hopefully because you're testing on x86 without gpio.");
		}
	}
	
	@AfterMethod
	public void tearDown()
	{
		if(turretControl != null)
		{
			turretControl.shutdown();
		}
	}
	
	@Test(groups={"G36FireControl"})
	public void safetySetTest()
	{
		Assert.assertTrue(turretControl.getSafety());
		turretControl.setSafety(true);
		Assert.assertTrue(turretControl.getSafety());
		
		turretControl.setSafety(false);
		Assert.assertFalse(turretControl.getSafety());
		
		turretControl.setSafety(true);
		Assert.assertTrue(turretControl.getSafety());
	}
	
	@Test(groups={"G36FireControl"}, expectedExceptions=NoAmmoException.class)
	public void cycleWithNoAmmoTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{
		turretControl.fire();
	}
	
	@Test(groups={"G36FireControl"}, expectedExceptions=SafetyEngagedException.class)
	public void cycleWithDefaultSafetyOnTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{
		Assert.assertFalse(turretControl.isChamberFilled());
		turretControl.setMagSize(30);
		turretControl.resetAmmoCount();
		turretControl.fire();
		Assert.assertFalse(turretControl.isChamberFilled());
	}
	
	@Test(groups={"G36FireControl"})
	public void cycleOnceSuccessfulTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{
		Assert.assertFalse(turretControl.isChamberFilled());
		turretControl.setMagSize(30);
		turretControl.resetAmmoCount();
		turretControl.setSafety(false);
		turretControl.fire();
		Assert.assertFalse(turretControl.isChamberFilled());
	}
	
	@Test(groups={"G36FireControl"}, expectedExceptions=NoAmmoException.class)
	public void depleteMagazineTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException, InterruptedException
	{
		int magSize = 4;
		
		Assert.assertFalse(turretControl.isChamberFilled());
		turretControl.setMagSize(magSize);
		turretControl.resetAmmoCount();
		turretControl.setSafety(false);
		
		int cyclesCompleted = 0;
		for(int i=0; i<magSize; i++)
		{
			Assert.assertEquals(cyclesCompleted, i);
			turretControl.fire();
			cyclesCompleted++;
			
			//ease up on the relay
			Thread.sleep(500);
		}	
		
		Assert.assertFalse(turretControl.isChamberFilled());
		Assert.assertEquals(turretControl.getAmmoCount(), 0);
		
		//throws expected no ammo exception
		turretControl.fire();
	}
	
	@Test(groups={"G36FireControl"}, expectedExceptions=NoAmmoException.class)
	public void depleteMagazineWithReloadTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException, InterruptedException
	{
		int magSize = 4;
		
		Assert.assertFalse(turretControl.isChamberFilled());
		turretControl.setMagSize(magSize);
		turretControl.resetAmmoCount();
		turretControl.setSafety(false);
		
		//deplete
		int cyclesCompleted = 0;
		for(int i=0; i<magSize; i++)
		{
			Assert.assertEquals(cyclesCompleted, i);
			turretControl.fire();
			cyclesCompleted++;
			
			//ease up on the relay
			Thread.sleep(500);
		}	
		
		Assert.assertFalse(turretControl.isChamberFilled());
		Assert.assertEquals(turretControl.getAmmoCount(), 0);
		
		try
		{
			//throws expected no ammo exception
			turretControl.fire();
		}
		catch(NoAmmoException e)
		{
			//good/expected
		}
		
		//reload
		turretControl.resetAmmoCount();
		
		//deplete again
		cyclesCompleted = 0;
		for(int i=0; i<magSize; i++)
		{
			Assert.assertEquals(cyclesCompleted, i);
			turretControl.fire();
			cyclesCompleted++;
			
			//ease up on the relay
			Thread.sleep(500);
		}	
		
		Assert.assertFalse(turretControl.isChamberFilled());
		Assert.assertEquals(turretControl.getAmmoCount(), 0);
		
		//throws expected no ammo exception
		turretControl.fire();
	}
}
