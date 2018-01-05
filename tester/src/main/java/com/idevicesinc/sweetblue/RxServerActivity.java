package com.idevicesinc.sweetblue;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.idevicesinc.sweetblue.rx.RxBleManager;
import com.idevicesinc.sweetblue.rx.RxBleServer;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.GattDatabase;
import com.idevicesinc.sweetblue.utils.Utils_Byte;
import com.idevicesinc.sweetblue.utils.Uuids;
import com.idevicesinc.sweetblue.tester.R;
import java.util.ArrayList;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class RxServerActivity extends Activity implements BleServer.IncomingListener
{


    private Button startAdvertising;
    private Button stopAdvertising;
    private ListView listView;
    private ClientAdaptor adaptor;

    private ArrayList<Client> clientList;

    private RxBleManager m_manager;
    private RxBleServer m_server;

    private GattDatabase m_db;

    private CompositeDisposable mDisposables = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        BleManagerConfig config = new BleManagerConfig();
        config.loggingEnabled = true;
        config.runOnMainThread = false;

        m_manager = RxBleManager.get(this, config);

        m_db = new GattDatabase().addService(Uuids.BATTERY_SERVICE_UUID)
                .addCharacteristic(Uuids.BATTERY_LEVEL).setPermissions().read().setProperties().read().notify_prop().completeService()
                .addService(Uuids.USER_DATA_SERVICE_UUID)
                .addCharacteristic(Uuids.USER_CONTROL_POINT).setPermissions().readWrite().setProperties().readWrite().completeService();

        m_server = m_manager.getServer(this, m_db, null);

        startAdvertising = findViewById(R.id.startAdvertising);
        startAdvertising.setOnClickListener(v ->
        {
            if (!m_server.isAdvertising())
            {
                final BleAdvertisingPacket packet = new BleAdvertisingPacket(Uuids.BATTERY_SERVICE_UUID);
                m_server.startAdvertising(packet).subscribe();
                startAdvertising.setEnabled(false);
                stopAdvertising.setEnabled(true);
            }
        });

        final Disposable d = m_server.observeStateEvents().observeOn(AndroidSchedulers.mainThread()).subscribe(e ->
        {
            String connectString;
            Client c = getClient(e.macAddress());
            if (e.didEnter(BleServerState.CONNECTED))
            {
                connectString = " got connected.";
                if (!clientList.contains(c))
                    clientList.add(c);
            }
            else if (e.didEnter(BleServerState.CONNECTING))
            {
                connectString = " is connecting...";
            }
            else if (e.didEnter(BleServerState.DISCONNECTED))
            {
                connectString = " became disconnected.";
            }
            else
            {
                connectString = " got an unknown connection state.";
            }
            c.log.append("[CONNECTION]: ").append("(").append(e.macAddress()).append(") ");
            c.log.append(connectString).append("\n");
            adaptor.notifyDataSetChanged();
        });

        mDisposables.add(d);

        stopAdvertising = findViewById(R.id.stopAdvertising);
        stopAdvertising.setOnClickListener(v ->
        {
            if (m_server.isAdvertising())
            {
                m_server.stopAdvertising();
                stopAdvertising.setEnabled(false);
                startAdvertising.setEnabled(true);
            }
        });

        listView = findViewById(R.id.listView);
        clientList = new ArrayList<>();
        adaptor = new ClientAdaptor(this, clientList);
        listView.setAdapter(adaptor);

        listView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            Client c = clientList.get(position);
            m_server.disconnect(c.macAddress);
            return true;
        });

        BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter()
        {
            @Override
            public Please onEvent(BluetoothEnablerEvent e)
            {
                Please p = super.onEvent(e);
                if (e.isDone())
                {
                    startAdvertising.setEnabled(true);
                }
                return p;
            }
        });
    }


    @Override
    public Please onEvent(IncomingEvent e)
    {
        runOnUiThread(() ->
        {
            String ending;
            if (e.type().isWrite())
            {
                ending = " with the data " + Utils_Byte.bytesToHexString(e.data_received()) + "\n";
            }
            else
            {
                ending = "\n";
            }
            // TODO - Store this somewhere
            Client c = getClient(e.macAddress());
            c.log.append("[INCOMING]: (").append(e.macAddress()).append(") ");
            c.log.append("Received ").append(e.type()).append(" on ").append(e.charUuid()).append(ending);
        });
        return Please.respondWithSuccess();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDisposables.dispose();
    }


    private Client getClient(String macAddress)
    {
        ArrayList<Client> list = new ArrayList<>(clientList);
        for (Client c : list)
        {
            if (c.macAddress.equals(macAddress))
            {
                return c;
            }
        }
        return new Client(macAddress);
    }


    private class ClientAdaptor extends ArrayAdapter<Client>
    {

        private ArrayList<Client> mClientList;


        public ClientAdaptor(Context context, ArrayList<Client> objects)
        {
            super(context, R.layout.server_clientitem_layout, objects);
            mClientList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder vh;
            if (convertView == null)
            {
                convertView = View.inflate(getContext(), R.layout.server_clientitem_layout, null);
                vh = new ViewHolder();
                vh.mac = convertView.findViewById(R.id.mac);
                vh.status = convertView.findViewById(R.id.state);
                convertView.setTag(vh);
            }
            else
            {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.mac.setText(mClientList.get(position).macAddress);
            vh.status.setText(getState(m_server.getBleServer().getStateMask(mClientList.get(position).macAddress)));
            return convertView;
        }

        private String getState(int stateMask)
        {
            if (BleServerState.CONNECTED.overlaps(stateMask))
            {
                return BleServerState.CONNECTED.name();
            }
            else if (BleServerState.CONNECTING.overlaps(stateMask))
            {
                return BleServerState.CONNECTING.name();
            }
            else
            {
                return BleServerState.DISCONNECTED.name();
            }
        }

        private final class ViewHolder
        {
            private TextView mac;
            private TextView status;
        }
    }

    private static final class Client
    {
        private final String macAddress;
        private final StringBuilder log = new StringBuilder();

        Client(String mac)
        {
            macAddress = mac;
        }

        @Override
        public int hashCode()
        {
            return macAddress.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Client)
            {
                return macAddress.equals(((Client) obj).macAddress);
            }
            return false;
        }
    }
}
