package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.BleServer;


public final class OutgoingException extends EventException
{

    public OutgoingException(BleServer.OutgoingListener.OutgoingEvent event)
    {
        super(event);
    }

    @Override
    public BleServer.OutgoingListener.OutgoingEvent getEvent()
    {
        return super.getEvent();
    }

}
