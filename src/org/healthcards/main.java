package org.healthcards;

//import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class main extends Activity {
    /** Called when the activity is first created. */
        
        private String qrtexto = "<cardHolder birthday='1980-01-01'><name title=''>homero2  simpson perez</name><document type='passport'>21312312</document><gender>male</gender><activity>mesero</activity><card cardType='a_card' creationDate='2013-09-10'><id>2</id><issuer isLab='false' id='1'>cancun</issuer></card></cardHolder>";
        private LocationManager locManager;
    	private LocationListener locListener;
    	private String lati = "", longi = "", vali;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        comenzarLocalizacion();
        }
    private void comenzarLocalizacion()
    {
    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	mostrarPosicion(loc);
    	
    	locListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		mostrarPosicion(location);
	    	}
	    	public void onProviderDisabled(String provider){
	
	    	}
	    	public void onProviderEnabled(String provider){
	
	    	}
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    	}
    	};
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
    	   	  	
    } 
    
     private void mostrarPosicion(Location loc) {
    	if(loc != null) {
    		lati = String.valueOf(loc.getLatitude());
        	longi = String.valueOf(loc.getLongitude());
        }
    	else {
    		}
    }
    //El evento del voton que se muestra en esta activity ejecuta este metodo.
    //y este metodo ejecuta la lib (QRlib) que permite escanear codigos QR con l camara del android. 
    public void scan_qr(View view) {
                /*Intent intent = new Intent("com.google.zxing.client.android.SCAN")

                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);*/
            
            
            String capturedQrValue = qrtexto;
                //Se llama al Activity que muestra los datos.
                Intent ver_data = new Intent(main.this, CardView.class);
                ver_data.putExtra("qr_xml", capturedQrValue);
                startActivity(ver_data);        
            
        }

    /*
     * Al terminar de escanear el codigo QR la este metodo recive los parametros.
     */
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                try {
                        if (resultCode == RESULT_OK) {
                                //El XML contenido en el codigo QR se pone en esta var.
                                String capturedQrValue = intent.getStringExtra("QR_string");
                                //Se llama al Activity que muestra los datos.
                                Intent ver_data = new Intent(main.this, CardView.class);
                                ver_data.putExtra("qr_xml", capturedQrValue);
                                startActivity(ver_data);                                        
                        } else {
                                Toast.makeText(this, "Lectura de tarjeta cancelada", Toast.LENGTH_LONG).show();
                        }
                } catch (Exception ex) {
                        Toast.makeText(this, "Ha ocurido un error al leer el codigo: " + ex.getMessage(),
                                        Toast.LENGTH_LONG).show();
                }
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_principal, menu);
                return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                case R.id.menu_configuracion:
                        Intent intent_configuracion = new Intent(this, Configuracion.class);
                        startActivityForResult(intent_configuracion, 1);
                        return true;
                default:
                        return super.onOptionsItemSelected(item);
                }
        }
}

