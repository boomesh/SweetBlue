package com.idevicesinc.sweetblue.rx;


import android.bluetooth.BluetoothGattService;
import com.idevicesinc.sweetblue.BleAdvertisingPacket;
import com.idevicesinc.sweetblue.BleNodeConfig;
import com.idevicesinc.sweetblue.BleServer;
import com.idevicesinc.sweetblue.BleServerState;
import com.idevicesinc.sweetblue.BleService;
import com.idevicesinc.sweetblue.annotations.Advanced;
import com.idevicesinc.sweetblue.rx.annotations.HotObservable;
import com.idevicesinc.sweetblue.rx.exception.AdvertisingException;
import com.idevicesinc.sweetblue.rx.exception.OutgoingException;
import com.idevicesinc.sweetblue.rx.exception.ServiceAddException;
import java.util.UUID;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;


public final class RxBleServer
{

    private final BleServer m_server;

    private Flowable<BleServer.StateListener.StateEvent> m_stateFlowable;
    private Flowable<BleServer.OutgoingListener.OutgoingEvent> m_outgoingFlowable;
    private Flowable<BleServer.ServiceAddListener.ServiceAddEvent> m_serviceAddFlowable;
    private Flowable<BleServer.AdvertisingListener.AdvertisingEvent> m_advertisingFlowable;


    private RxBleServer(BleServer server)
    {
        m_server = server;
    }


    public final BleServer getBleServer()
    {
        return m_server;
    }


    public final @HotObservable Flowable<BleServer.StateListener.StateEvent> observeStateEvents()
    {
        if (m_stateFlowable == null)
        {
            m_stateFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.StateListener.StateEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.StateListener.StateEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_server.setListener_State(new BleServer.StateListener()
                    {
                        @Override
                        public void onEvent(StateEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_server.setListener_State(null);
                            m_stateFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_stateFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.OutgoingListener.OutgoingEvent> observeOutgoingEvents()
    {
        if (m_outgoingFlowable == null)
        {
            m_outgoingFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.OutgoingListener.OutgoingEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.OutgoingListener.OutgoingEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_server.setListener_Outgoing(new BleServer.OutgoingListener()
                    {
                        @Override
                        public void onEvent(OutgoingEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_server.setListener_Outgoing(null);
                            m_outgoingFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_outgoingFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.ServiceAddListener.ServiceAddEvent> observeServiceAddEvents()
    {
        if (m_serviceAddFlowable == null)
        {
            m_serviceAddFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.ServiceAddListener.ServiceAddEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.ServiceAddListener.ServiceAddEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_server.setListener_ServiceAdd(new BleServer.ServiceAddListener()
                    {
                        @Override
                        public void onEvent(ServiceAddEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_server.setListener_ServiceAdd(null);
                            m_serviceAddFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_serviceAddFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.AdvertisingListener.AdvertisingEvent> observeAdvertisingEvents()
    {
        if (m_advertisingFlowable == null)
        {
            m_advertisingFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.AdvertisingListener.AdvertisingEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.AdvertisingListener.AdvertisingEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_server.setListener_Advertising(new BleServer.AdvertisingListener()
                    {
                        @Override
                        public void onEvent(AdvertisingEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_server.setListener_Advertising(null);
                            m_advertisingFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_advertisingFlowable.share();
    }

    public final void setConfig(BleNodeConfig config)
    {
        m_server.setConfig(config);
    }

    public final Single<BleServer.OutgoingListener.OutgoingEvent> sendIndication(final String macAddress, final UUID serviceUuid, final UUID charUuid, final byte[] data)
    {
        return Single.create(new SingleOnSubscribe<BleServer.OutgoingListener.OutgoingEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<BleServer.OutgoingListener.OutgoingEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_server.sendIndication(macAddress, serviceUuid, charUuid, data, new BleServer.OutgoingListener()
                {
                    @Override
                    public void onEvent(OutgoingEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new OutgoingException(e));
                    }
                });
            }
        });
    }

    public final Single<BleServer.OutgoingListener.OutgoingEvent> sendNotification(final String macAddress, final UUID serviceUuid, final UUID charUuid, final byte[] data)
    {
        return Single.create(new SingleOnSubscribe<BleServer.OutgoingListener.OutgoingEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<BleServer.OutgoingListener.OutgoingEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_server.sendNotification(macAddress, serviceUuid, charUuid, data, new BleServer.OutgoingListener()
                {
                    @Override
                    public void onEvent(OutgoingEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new OutgoingException(e));
                    }
                });
            }
        });
    }

    public final Completable connect(final String macAddress, final BleServer.ConnectionFailListener failListener)
    {
        return Completable.create(new CompletableOnSubscribe()
        {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_server.connect(macAddress, new BleServer.StateListener()
                {
                    @Override
                    public void onEvent(StateEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.didEnter(BleServerState.CONNECTED))
                        {
                            emitter.onComplete();
                        }
                    }
                }, failListener);
            }
        });
    }

    public final boolean disconnect(String macAddress)
    {
        return m_server.disconnect(macAddress);
    }

    public final void disconnect()
    {
        m_server.disconnect();
    }

    public final Single<BleServer.ServiceAddListener.ServiceAddEvent> addService(final BleService service)
    {
        return Single.create(new SingleOnSubscribe<BleServer.ServiceAddListener.ServiceAddEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<BleServer.ServiceAddListener.ServiceAddEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_server.addService(service, new BleServer.ServiceAddListener()
                {
                    @Override
                    public void onEvent(ServiceAddEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new ServiceAddException(e));
                    }
                });
            }
        });
    }

    public final BluetoothGattService removeService(UUID serviceUuid)
    {
        return m_server.removeService(serviceUuid);
    }

    public final void removeAllServices()
    {
        m_server.removeAllServices();
    }

    public final String getMacAddress()
    {
        return m_server.getMacAddress();
    }

    public final String getName()
    {
        return m_server.getName();
    }

    public final boolean isAdvertising()
    {
        return m_server.isAdvertising();
    }

    @Advanced
    public final void setName(String name)
    {
        m_server.setName(name);
    }

    public final Observable<String> getClients()
    {
        return Observable.fromIterable(m_server.getClients_List());
    }

    public final Single<RxAdvertisingEvent> startAdvertising(final BleAdvertisingPacket advPacket)
    {
        return Single.create(new SingleOnSubscribe<RxAdvertisingEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<RxAdvertisingEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_server.startAdvertising(advPacket, new BleServer.AdvertisingListener()
                {
                    @Override
                    public void onEvent(AdvertisingEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(new RxAdvertisingEvent(e));
                        else
                            emitter.onError(new AdvertisingException(e));
                    }
                });
            }
        });
    }

    public final void stopAdvertising()
    {
        m_server.stopAdvertising();
    }






    static RxBleServer create(BleServer server)
    {
        return new RxBleServer(server);
    }
}
