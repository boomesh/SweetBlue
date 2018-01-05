package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.BleServer;

public final class ServiceAddException extends EventException
{

    public ServiceAddException(BleServer.ServiceAddListener.ServiceAddEvent event)
    {
        super(event);
    }

    @Override
    public BleServer.ServiceAddListener.ServiceAddEvent getEvent()
    {
        return super.getEvent();
    }

}
