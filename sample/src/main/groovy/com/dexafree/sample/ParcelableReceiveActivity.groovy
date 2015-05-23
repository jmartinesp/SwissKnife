package com.dexafree.sample
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.arasthel.swissknife.annotations.InjectView

public class ParcelableReceiveActivity extends Activity {

    @InjectView(R.id.guys_amount)
    TextView guysAmount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcelable_receive)

        def i = getIntent()
        Team team = (Team)i.getParcelableExtra("team")
        Log.d("Got it?", team.guys.size())
        guysAmount.setText(team.guys.size())
    }
}
