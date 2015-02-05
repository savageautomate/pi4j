package com.pi4j.i2c.devices.mcp45xx_mcp46xx;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pi4j.i2c.devices.mcp45xx_mcp46xx.PotentiometerImpl.NonVolatileMode;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: I2C Device Abstractions
 * FILENAME      :  MCP45xxMCP46xxPotentiometerTest.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2013 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Test for abstract Pi4J-device for MCP45XX and MCP46XX ICs.
 * 
 * @see PotentiometerImpl
 * @author <a href="http://raspelikan.blogspot.co.at">Raspelikan</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class PotentiometerImplStaticTest {
	
	@Mock
	private I2CDevice i2cDevice;
	
	@Mock
	private I2CBus i2cBus;
	
	@Mock
	private DeviceController controller;
	
	@Mock
	private DeviceControllerFactory controllerFactory;

	/**
	 * publishes some internals for testing purposes
	 */
	static class TestablePotentiometer
			extends PotentiometerImpl {

		private boolean capableOfNonVolatileWiper = false;
		
		TestablePotentiometer(I2CBus i2cBus, boolean pinA0,
				boolean pinA1, boolean pinA2, Channel channel,
				NonVolatileMode nonVolatileMode,
				DeviceControllerFactory controllerFactory)
				throws IOException {
			
			super(i2cBus, pinA0, pinA1, pinA2, channel,
					nonVolatileMode, 0, controllerFactory);
			
		}
		
		public TestablePotentiometer(I2CBus i2cBus,
				Channel channel, NonVolatileMode nonVolatileMode,
				int initialValueForVolatileWipers)
				throws IOException {
			
			super(i2cBus, false, false, false, channel, nonVolatileMode,
					initialValueForVolatileWipers);
					
		}
		
		public void initialize(final int initialValueForVolatileWipers) throws IOException {
			super.initialize(initialValueForVolatileWipers);
		}
		
		@Override
		public boolean isCapableOfNonVolatileWiper() {
			return capableOfNonVolatileWiper;
		}
		
		public void setCapableOfNonVolatileWiper(
				boolean capableOfNonVolatileWiper) {
			this.capableOfNonVolatileWiper = capableOfNonVolatileWiper;
		}
		
		@Override
		public int getMaxValue() {
			return 256;
		}

		@Override
		public boolean isRheostat() {
			return false;
		}
		
	}
	
	@Before
	public void initialize() throws IOException {
		
		when(i2cBus.getDevice(anyInt()))
				.thenReturn(i2cDevice);
		
		when(controllerFactory.getController(any(I2CDevice.class)))
				.thenReturn(controller);
		
	}
	
	@Test
	public void testCreation() throws IOException {
		
		// wrong parameters
		
		try {
			new TestablePotentiometer(null,
					false, false, false, Channel.A,
					NonVolatileMode.VOLATILE_ONLY, controllerFactory);
			fail("Got no RuntimeException on constructing "
					+ "a PotentiometerImpl using a null-I2CBus");
		} catch (RuntimeException e) {
			// expected expection
		}
		
		try {
			new TestablePotentiometer(i2cBus,
					false, false, false, null,
					NonVolatileMode.VOLATILE_ONLY, controllerFactory);
			fail("Got no RuntimeException on constructing "
					+ "a PotentiometerImpl using a null-Channel");
		} catch (RuntimeException e) {
			// expected expection
		}

		try {
			new TestablePotentiometer(i2cBus,
					false, false, false, Channel.A,
					null, controllerFactory);
			fail("Got no RuntimeException on constructing "
					+ "a PotentiometerImpl using a null-NonVolatileMode");
		} catch (RuntimeException e) {
			// expected expection
		}
		
		try {
			new TestablePotentiometer(i2cBus,
					false, false, false, Channel.A, NonVolatileMode.VOLATILE_ONLY, null);
			fail("Got no RuntimeException on constructing "
					+ "a PotentiometerImpl using a null-controllerFactory");
		} catch (RuntimeException e) {
			// expected expection
		}

		// correct parameters
		
		new TestablePotentiometer(i2cBus,
				false, false, false, Channel.A, NonVolatileMode.VOLATILE_ONLY,
				controllerFactory);
		
		new TestablePotentiometer(i2cBus, Channel.B,
				NonVolatileMode.VOLATILE_ONLY, 127);
		
	}
	
	@Test
	public void testBuildI2CAddress() throws IOException {
		
		int address1 = PotentiometerImpl.buildI2CAddress(false, false, false);
		assertEquals("'buildI2CAddress(false, false, false)' "
				+ "does not return '0b01010000'", 0b01010000, address1);

		int address2 = PotentiometerImpl.buildI2CAddress(true, false, false);
		assertEquals("'buildI2CAddress(true, false, false)' "
				+ "does not return '0b01010010'", 0b01010010, address2);
		
		int address3 = PotentiometerImpl.buildI2CAddress(true, true, false);
		assertEquals("'buildI2CAddress(true, true, false)' "
				+ "does not return '0b01010110'", 0b01010110, address3);

		int address4 = PotentiometerImpl.buildI2CAddress(true, true, true);
		assertEquals("'buildI2CAddress(true, true, true)' "
				+ "does not return '0b01011110'", 0b01011110, address4);
		
	}
	
	@Test
	public void testInitialization() throws IOException {
		
		final TestablePotentiometer poti
				= new TestablePotentiometer(i2cBus,
					false, false, false, Channel.A, NonVolatileMode.VOLATILE_ONLY,
					controllerFactory);
		
		reset(controller);
		
		poti.setCapableOfNonVolatileWiper(true);
		poti.initialize(0);
		
		// called with expected parameters
		verify(controller).getValue(
				DeviceControllerChannel.A
				, false);
		// only called with expected parameters
		verify(controller, times(1)).getValue(
				any(DeviceControllerChannel.class)
				, anyBoolean());
		// never called since non-volatile-wiper is true
		verify(controller, times(0)).setValue(
				any(DeviceControllerChannel.class)
				, anyInt(), anyBoolean());
		
		reset(controller);
		
		poti.setCapableOfNonVolatileWiper(false);
		poti.initialize(120);
		
		// called with expected parameters
		verify(controller).setValue(
				DeviceControllerChannel.A
				, 120, false);
		// only called with expected parameters
		verify(controller, times(1)).setValue(
				any(DeviceControllerChannel.class)
				, anyInt(), anyBoolean());
		// never called since non-volatile-wiper is true
		verify(controller, times(0)).getValue(
				DeviceControllerChannel.A
				, true);

	}
	
	@Test
	public void testToString() throws IOException {
		
		when(controller.toString()).thenReturn("ControllerMock");
		
		final String toString = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.VOLATILE_ONLY, controllerFactory).toString();
		
		assertNotNull("result of 'toString()' is null!", toString);
		assertEquals("Unexpected result from calling 'toString'!",
				"com.pi4j.i2c.devices.mcp45xx_mcp46xx.PotentiometerImplStaticTest$TestablePotentiometer{\n"
				+ "  channel='com.pi4j.i2c.devices.mcp45xx_mcp46xx.Channel.A',\n"
				+ "  controller='ControllerMock',\n"
				+ "  nonVolatileMode='VOLATILE_ONLY',\n"
				+ "  currentValue='0'\n}",
				toString);
		
	}
	
	@Test
	public void testEquals() throws IOException {
		
		final TestablePotentiometer poti = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.VOLATILE_ONLY, controllerFactory);
		final TestablePotentiometer copyOfPoti = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.VOLATILE_ONLY, controllerFactory);

		final TestablePotentiometer other1 = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.B, NonVolatileMode.VOLATILE_ONLY, controllerFactory);
		final TestablePotentiometer other2 = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.NONVOLATILE_ONLY, controllerFactory);
		final TestablePotentiometer other3 = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.NONVOLATILE_ONLY, controllerFactory);
		other3.setCurrentValue(127);
		
		controller = mock(DeviceController.class);
		when(controllerFactory.getController(any(I2CDevice.class)))
				.thenReturn(controller);
		final TestablePotentiometer other4 = new TestablePotentiometer(i2cBus, false, false, false,
				Channel.A, NonVolatileMode.VOLATILE_ONLY, controllerFactory);
		
		assertNotEquals("'poti.equals(null)' returns true!",
				poti, null);
		assertEquals("'poti.equals(poti) returns false!",
				poti, poti);
		assertNotEquals("'poti.equals(\"Test\")' returns true!",
				poti, "Test");
		assertEquals("'poti.equals(copyOfPoti)' returns false!",
				poti, copyOfPoti);
		assertNotEquals("'poti.equals(other1)' returns true!",
				poti, other1);
		assertEquals("'poti.equals(other2)' returns false!",
				poti, other2);
		assertEquals("'poti.equals(other3)' returns false!",
				poti, other3);
		assertNotEquals("'poti.equals(other4)' returns true!",
				poti, other4);
		
	}
	
}