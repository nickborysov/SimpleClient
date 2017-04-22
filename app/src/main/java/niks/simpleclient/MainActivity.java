package niks.simpleclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * MainActivity
 * <p>
 * Activity for start and stop messaging with server. On click button start,
 * execute Client for connect to server in other thread.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set OnClickListener for buttons
        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);

        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);

        // Set default address to EditText
        EditText editText = (EditText) findViewById(R.id.editTextHostName);
        editText.setText("192.168.0.65:56001");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // If button Start clicked
            case R.id.buttonStart:
                // Parse ip and port from EditText, can be exception,
                // because have not check for input data
                String hostName = ((EditText) findViewById(R.id.editTextHostName))
                        .getText().toString();
                String ipAddress = hostName.substring(0, hostName.indexOf(":"));
                int port = Integer.parseInt(hostName.substring(hostName.indexOf(":") + 1));

                // If Client used cancel previous task
                if (mClient != null) {
                    mClient.cancel(false);
                }
                // Create new Client and execute in other thread
                mClient = new Client(this, ipAddress, port);
                mClient.execute();

                // Set ListView with messages from server
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, mClient.getMessagesFromServer());
                ListView listView = (ListView) findViewById(R.id.listViewMessageFromServer);
                listView.setAdapter(adapter);
                break;
            // If button Stop clicked
            case R.id.buttonStop:
                // Cancel client
                if (mClient != null) {
                    mClient.cancel(false);
                }
                setClientInfo("");
                break;
        }
    }

    /**
     * Notify ArrayAdapter about ArrayList changed
     */
    public void onListViewUpdate() {
        ((ArrayAdapter) (((ListView) findViewById(R.id.listViewMessageFromServer))
                .getAdapter())).notifyDataSetChanged();
    }

    /**
     * Set message to textView
     * @param message text of message
     */
    public void setClientInfo(String message) {
        TextView textView = (TextView) findViewById(R.id.textViewClientInfo);
        textView.setText(message);
    }

}
