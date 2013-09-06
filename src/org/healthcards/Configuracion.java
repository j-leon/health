package org.healthcards;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Configuracion extends PreferenceActivity {	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("org_healthcards");     
        addPreferencesFromResource(R.layout.configuracion);
    }	
		
}
