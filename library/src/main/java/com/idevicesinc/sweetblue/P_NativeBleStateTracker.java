package com.idevicesinc.sweetblue;

import com.idevicesinc.sweetblue.NativeManagerStateListener.NativeStateEvent;


final class P_NativeBleStateTracker extends PA_StateTracker
{
	private NativeManagerStateListener m_stateListener;
	private final BleManager m_mngr;
	
	P_NativeBleStateTracker(BleManager mngr)
	{
		super(BleManagerState.VALUES());
		
		m_mngr = mngr;
	}
	
	public void setListener(NativeManagerStateListener listener)
	{
		m_stateListener = listener;
	}

	@Override protected void onStateChange(int oldStateBits, int newStateBits, int intentMask, int status)
	{
		if( m_stateListener != null )
		{
			final NativeStateEvent event = new NativeStateEvent(m_mngr, oldStateBits, newStateBits, intentMask);

			m_stateListener.onEvent(event);
		}
	}
}
