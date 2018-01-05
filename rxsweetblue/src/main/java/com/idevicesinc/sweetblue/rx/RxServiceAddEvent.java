package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleServer;

public final class RxServiceAddEvent
{

    private final BleServer.ServiceAddListener.ServiceAddEvent m_event;


    RxServiceAddEvent(BleServer.ServiceAddListener.ServiceAddEvent event)
    {
        m_event = event;
    }


    public final BleServer.ServiceAddListener.ServiceAddEvent event()
    {
        return m_event;
    }

    public final boolean wasSuccess()
    {
        return m_event.wasSuccess();
    }

    public final RxBleServer server()
    {
        return RxBleManager.getOrCreateServer(m_event.server());
    }

}
