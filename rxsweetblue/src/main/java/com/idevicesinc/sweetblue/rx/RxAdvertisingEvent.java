package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleServer;


public final class RxAdvertisingEvent
{

    private final BleServer.AdvertisingListener.AdvertisingEvent m_event;


    RxAdvertisingEvent(BleServer.AdvertisingListener.AdvertisingEvent event)
    {
        m_event = event;
    }


    public final BleServer.AdvertisingListener.AdvertisingEvent event()
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
