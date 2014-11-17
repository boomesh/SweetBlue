package com.idevicesinc.sweetblue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import com.idevicesinc.sweetblue.BleDevice.StateListener;
import com.idevicesinc.sweetblue.BleManagerConfig.ReconnectRateLimiter;
import com.idevicesinc.sweetblue.utils.BitwiseEnum;

/**
 * An enumeration of the various states that a {@link BleDevice} can be in.
 * Note that a device can and usually will be in multiple states simultaneously.
 * Use {@link BleDevice#setListener_State(StateListener)} to be notified of state changes.
 * 
 * @see BleDevice.StateListener
 * 
 * @author dougkoellmer
 */
public enum DeviceState implements BitwiseEnum
{
	/**
	 * The device has been undiscovered and you should have been notified through {@link BleManager.DiscoveryListener#onDeviceUndiscovered(BleDevice)}.
	 * This means the object is effectively dead. {@link BleManager} has removed all references to it and you should do the same.
	 */
	UNDISCOVERED,
	
	/**
	 * If {@link BleManagerConfig#reconnectRateLimiter} is set and the device implicitly disconnects, either through going out of range,
	 * signal disruption, or whatever, then the device will enter this state. It will continue in this state until you return
	 * {@link BleManagerConfig.ReconnectRateLimiter#CANCEL} from {@link BleManagerConfig.ReconnectRateLimiter#getTimeToNextReconnect(BleDevice, int, Interval, Interval)}
	 * or call {@link BleDevice#disconnect()} or when the device actually successfully reconnects. 
	 * 
	 */
	ATTEMPTING_RECONNECT,
	
	/**
	 * The device will always be in this state unless it becomes {@link #UNDISCOVERED}.
	 */
	DISCOVERED,
	
	/**
	 * The device will always be in this state while {@link #CONNECTED} is not active. Note that this doesn't *necessarily* mean that the actual
	 * physical device is advertising, just that it is assumed to be so.
	 */
	ADVERTISING,
	
	/**
	 * The device will always be in this state while {@link #CONNECTED} is not active. Analogous to {@link BluetoothProfile#STATE_DISCONNECTED}.
	 */
	DISCONNECTED,
	
	/**
	 * Analogous to {@link BluetoothDevice#BOND_NONE}. May not be relevant for your application if you don't use encrypted characteristics.
	 */
	UNBONDED,
	
	/**
	 * Analogous of {@link BluetoothDevice#BOND_BONDING}. May not be relevant for your application if you don't use encrypted characteristics.
	 */
	BONDING,
	
	/**
	 * Analogous of {@link BluetoothDevice#BOND_BONDED}. May not be relevant for your application if you don't use encrypted characteristics.
	 */
	BONDED,
	
	/**
	 * A convenience flag for checking if the device is connecting in an overall sense. This state is active if any one of {@link #CONNECTING},
	 * {@link #GETTING_SERVICES}, {@link #AUTHENTICATING}, or {@link #INITIALIZING} is also active.
	 */
	CONNECTING_OVERALL,
	
	/**
	 * Analogous to {@link BluetoothProfile#STATE_CONNECTING}. If this state is active then we're establishing an actual BLE connection.
	 */
	CONNECTING,
	
	/**
	 * Analogous to {@link BluetoothProfile#STATE_CONNECTED}. Once this state becomes active we don't consider ourselves "fully" connected
	 * because we still generally have to discover services and maybe do a few reads or writes to initialize things. So generally speaking
	 * no actual action should be taken when the {@link BleDevice} becomes {@link #CONNECTED}. Instead it's best to listen for {@link #INITIALIZED}.
	 */
	CONNECTED,
	
	/**
	 * This state is active while we request a list of services from the native stack after becoming {@link #CONNECTED}.
	 */
	GETTING_SERVICES,
	
	/**
	 * This state can only become active if you use {@link BleDevice#connectAndAuthenticate(BleTransaction)} or {@link BleDevice#connect(BleTransaction, BleTransaction)}
	 * to start a connection with an authentication transaction.
	 */
	AUTHENTICATING,
	
	/**
	 * This state becomes active either if the {@link BleTransaction} provided to {@link BleDevice#connectAndAuthenticate(BleTransaction)} or
	 * {@link BleDevice#connect(BleTransaction, BleTransaction)} succeeds with {@link BleTransaction#succeed()}, OR if you use 
	 * {@link BleDevice#connect()} or {@link BleDevice#connectAndInitialize(BleTransaction)} - i.e. you connect without authentication.
	 * In the latter case the {@link #AUTHENTICATING} state is skipped and we go straight to being implicitly {@link #AUTHENTICATED}.
	 */
	AUTHENTICATED,
	
	/**
	 * This state can only become active if you use {@link BleDevice#connectAndInitialize(BleTransaction)} or {@link BleDevice#connect(BleTransaction, BleTransaction)}
	 * to start a connection with an initialization transaction.
	 */
	INITIALIZING,
	
	/**
	 * This is generally the state you want to listen for to consider your {@link BleDevice} "fully" connected and ready to go, instead of
	 * basing it off of just {@link #CONNECTED}.
	 * <br><br>
	 * This state becomes active either if the {@link BleTransaction} provided to {@link BleDevice#connectAndInitialize(BleTransaction)} or
	 * {@link BleDevice#connect(BleTransaction, BleTransaction)} succeeds with {@link BleTransaction#succeed()}, OR if you use 
	 * {@link BleDevice#connect()} or {@link BleDevice#connectAndAuthenticate(BleTransaction)} - i.e. you connect without authentication.
	 * In the latter case the {@link #INITIALIZING} state is skipped and we go straight to being implicitly {@link #INITIALIZED}.
	 */
	INITIALIZED,
	
	/**
	 * This state becomes active when you call {@link BleDevice#updateFirmware(BleTransaction)} and remains active until the provided
	 * {@link BleTransaction} calls {@link BleTransaction#succeed()} or {@link BleTransaction#fail()} (or of course if your {@link BleDevice}
	 * becomes {@link #DISCONNECTED}).
	 */
	UPDATING_FIRMWARE;
	
	static final int PURGEABLE_MASK = DISCOVERED.bit() | DISCONNECTED.bit() | UNBONDED.bit() | BONDING.bit() | BONDED.bit() | ADVERTISING.bit();
	
	@Override public boolean overlaps(int mask)
	{
		return (bit() & mask) != 0x0;
	}
	
	@Override public int bit()
	{
		return 1 << (ordinal() + 1);
	}
	
	/**
	 * Given an old and new state mask from {@link StateListener#onStateChange(BleDevice, int, int)}, this
	 * method tells you whether the 'this' state was appended.
	 * 
	 * @see #wasExited(int, int)
	 */
	public boolean wasEntered(int oldStateBits, int newStateBits)
	{
		return !this.overlaps(oldStateBits) && this.overlaps(newStateBits);
	}
	
	/**
	 * Reverse of {@link #wasEntered(int, int)}.
	 * 
	 * @see #wasEntered(int, int)
	 */
	public boolean wasExited(int oldStateBits, int newStateBits)
	{
		return this.overlaps(oldStateBits) && !this.overlaps(newStateBits);
	}
	
	/**
	 * A convenience for UI purposes, this returns the "highest" connection state representing
	 * a transition from one state to another, so something with "ING" in the name (except {@link #UPDATING_FIRMWARE}).
	 * Chronologically this method returns {@link #CONNECTING}, {@link #GETTING_SERVICES},
	 * {@link #AUTHENTICATING} (if {@link BleDevice#connectAndAuthenticate(BleTransaction)} or 
	 * {@link BleDevice#connect(BleTransaction, BleTransaction)} is called), {@link #BONDING} (if relevant),
	 * and {@link #INITIALIZING}  (if {@link BleDevice#connectAndInitialize(BleTransaction)} or 
	 * {@link BleDevice#connect(BleTransaction, BleTransaction)} is called).
	 * 
	 * @param stateMask Generally the value returned by {@link BleDevice#getStateMask()}.
	 */
	public static DeviceState getTransitoryConnectionState(int stateMask)
	{
		if( CONNECTED.overlaps(stateMask) )
		{
			if( INITIALIZING.overlaps(stateMask) )		return INITIALIZING;
			if( BONDING.overlaps(stateMask) )			return BONDING;
			if( AUTHENTICATING.overlaps(stateMask) )	return AUTHENTICATING;
			if( GETTING_SERVICES.overlaps(stateMask) )	return GETTING_SERVICES;
		}
		else
		{
			if( CONNECTING.overlaps(stateMask) )		return CONNECTING;
		}
		
		return null;
	}
}