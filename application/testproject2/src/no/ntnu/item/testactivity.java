package no.ntnu.item;

import android.app.Activity;
import android.os.Bundle;

public class testactivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.out.println("Hello World");
        setContentView(R.layout.main);
    }
}