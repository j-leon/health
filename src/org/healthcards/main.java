package org.healthcards;

import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    //El evento del voton que se muestra en esta activity ejecuta este metodo.
    //y este metodo ejecuta la lib (QRlib) que permite escanear codigos QR con l camara del android. 
    public void scan_qr(View view) {
		Intent intent = new Intent(main.this,
				CaptureActivity.class);

		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
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