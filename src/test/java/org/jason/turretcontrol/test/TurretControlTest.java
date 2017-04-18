package org.jason.turretcontrol.test;

import java.util.Collections;
import java.util.HashSet;

import org.jason.turretcontrol.TurretControl;
import org.jason.turretcontrol.config.ConfigLoader;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
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
	
	@Test(groups={"TurretControl"})
	public void supportedMagazinesBasicTest() 
	{
		String[] foundMagazines = turretControl.getSupportedMagazines();
		Assert.assertEquals(2, foundMagazines.length);
		
		HashSet<String> magazines = new HashSet<String>();
		
		magazines.add(foundMagazines[0]);
		magazines.add(foundMagazines[1]);
			
		Assert.assertTrue(magazines.contains("NStrikeStandard"));
		Assert.assertTrue(magazines.contains("NStrikeExtended"));
	}
	
	
	
	
	
}
