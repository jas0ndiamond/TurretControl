package test.java.turretcontrol;

import main.java.org.jason.turretcontrol.TurretControl;
import main.java.org.jason.turretcontrol.config.ConfigLoader;
import main.java.org.jason.turretcontrol.exception.JamOccurredException;
import main.java.org.jason.turretcontrol.exception.NoAmmoException;
import main.java.org.jason.turretcontrol.exception.SafetyEngagedException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TurretControlTest {

	private TurretControl turretControl;
	
	@BeforeTest
	public void setupProperties()
	{
		
	}
	
	@BeforeMethod
	public void setupTest() throws Exception
	{	
		//use mock fire control
		String tcConfig = ConfigLoader.getConfigJSONObject("src/test/java/mockTurretConfig.json")
				.getString("turret");
		
		turretControl = new TurretControl(tcConfig);
	}
	
	@Test(groups={"TurretControl"})
	public void defaultSafetyTest()
	{
		Assert.assertTrue(turretControl.getSafety());
	}
	
	@Test(groups={"TurretControl"})
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
	
	@Test(groups={"TurretControl"})
	public void ammoCountTest()
	{
		turretControl.setMagSize(30);
		
		//Default ammo count
		Assert.assertEquals(turretControl.getAmmoCount(), TurretControl.DEFAULT_AMMO_COUNT);
		
		turretControl.setAmmoCount(30);
		
		Assert.assertEquals(turretControl.getAmmoCount(), 30);
		
		turretControl.setAmmoCount(0);
		Assert.assertEquals(turretControl.getAmmoCount(), 0);
		
		turretControl.resetAmmoCount();
		Assert.assertEquals(turretControl.getAmmoCount(), 30);
	}
	
	@Test(groups={"TurretControl"})
	public void magSizeTest()
	{
		//Default mag size
		Assert.assertEquals(turretControl.getMagSize(), TurretControl.DEFAULT_MAG_SIZE);
		
		turretControl.setMagSize(30);
		
		Assert.assertEquals(turretControl.getMagSize(), 30);
	}
	
	@Test(groups={"TurretControl"})
	public void chamberTest() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{
		Assert.assertFalse(turretControl.isChamberFilled());
		turretControl.setMagSize(30);
		turretControl.resetAmmoCount();
		turretControl.setSafety(false);
		turretControl.fire();
		Assert.assertFalse(turretControl.isChamberFilled());
	}
}
