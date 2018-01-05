package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.BleServer;


public final class AdvertisingException extends EventException
{

    public AdvertisingException(BleServer.AdvertisingListener.AdvertisingEvent event)
    {
        super(event);
    }

    @Override
    public BleServer.AdvertisingListener.AdvertisingEvent getEvent()
    {
        return super.getEvent();
    }

}
