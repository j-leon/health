package org.healthcards;

import org.healthcards.CustomHttpClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import xml.XMLParser;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Address;  
import android.location.Geocoder; 
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CardView extends Activity {
        private SharedPreferences config;
        private HashMap<String, String>        card_data;
        private String fecha, hora;
        private LocationManager locManager;
    	private LocationListener locListener;
    	private String lati = "", longi = "", vali;
    	private String gps;
        private Context context;
        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_card);      
        context=this;
        try {
                //Esta actividad es llamda desde la principal y recive como parametro el XML que se escaneo de la tarjeta.
                        Bundle parametros = this.getIntent().getExtras();
                        String xml = parametros.getString("qr_xml");
                        this._get_card_data_from_xml(xml);
                        this.show_data_from_xml();
                } catch (Exception ex) {
                                Toast.makeText(this, "Error al mostrar los datos de la terjeta", Toast.LENGTH_LONG).show();
                }
        }
        //El XML que se recive como parametro es parseado usando la clase XMLParser.
        private void _get_card_data_from_xml(String xml) throws Exception{
                XMLParser parser = new XMLParser(xml);
                //Los datos parseados son puestos en el atributo card_data. 
                this.card_data = parser.getDataFromXML();
        }
        //Los datos que existen en el atributo card_data son mostrados en la activity.
        public void show_data_from_xml() {                         
                try {                        
                        TextView tv_name = (TextView) findViewById(R.id.text_name);
                        String title = card_data.get("title");
                        String title_str = title.length() > 1 ? title + " " : ""; 
                        tv_name.setText(title_str + card_data.get("name"));
                        
                        TextView tv_gender = (TextView)findViewById(R.id.text_gender);
                        tv_gender.setText(card_data.get("gender").equals("female") ? R.string.gender_female : R.string.gender_male);
                        
                        TextView tv_creation_date = (TextView)findViewById(R.id.text_card_cd);
                        tv_creation_date.setText(card_data.get("card_creationDate"));
                        
                        TextView tv_docID = (TextView) findViewById(R.id.text_docID);
                        tv_docID.setText(card_data.get("document_id") + " (" + card_data.get("document_type") + ")");
                        
                        TextView tv_cardType = (TextView) findViewById(R.id.text_cardType);
                        String card_type = card_data.get("cardType");
                        if(card_type.equals("a_card"))
                                card_type = "A";
                        else
                                card_type = "B";
                        tv_cardType.setText(card_type);
                        
                        TextView tv_card_id = (TextView) findViewById(R.id.text_card_id);
                        tv_card_id.setText(card_data.get("card_id"));
                        
                        String issued_by_lab = card_data.get("issuer_is_lab");
                        String label_issuer = issued_by_lab.equals("true") ? "Lab: " : "Health Center: ";
                        
                TextView tv_issuer = (TextView)findViewById(R.id.text_card_issuer);
                tv_issuer.setText(label_issuer + card_data.get("card_issuer"));
                        
                        
                } catch (Exception e) {
                        Toast.makeText(this, "Ha ocurrido un error al mostrar los datos, intenetelo de nuevo",
                                        Toast.LENGTH_LONG).show();
                }
        }        
        //Al pulsar el boton correspondiente se consulta el estado de la tarjeta y se muestra.
        public void get_card_state(View view){
                String card_id = card_data.get("card_id");
                TextView tv_card_state = (TextView) findViewById(R.id.text_card_state);
                try {                        
                        String card_state = this._get_card_state_from_openerp(card_id);
                        tv_card_state.setText(card_state.toUpperCase());
                        AlertDialog.Builder alerta_ok = new AlertDialog.Builder(this);
                        alerta_ok.setTitle("Tarjeta encontrada");
                        alerta_ok.setMessage("Estado de la tarjeta: " + card_state)
                        .setCancelable(false)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {}
                          });        
                        alerta_ok.show();
                } catch (Exception ex) {
                        tv_card_state.setText("Unknow");
                        AlertDialog.Builder alerta_error = new AlertDialog.Builder(this);
                        alerta_error.setTitle("Error");
                        alerta_error.setMessage(ex.getMessage())
                        .setCancelable(false)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {}
                          });                        
                        alerta_error.show();
                }
        }
        
        /*
         * El estado de la tarjeta es consultado directamente en OpenERP via RPC
         * usando la lib xmlrpc.android disponible en internet
         */
        private String _get_card_state_from_openerp(String card_id) throws Exception {
                config = this.getSharedPreferences("org_healthcards", 0);
                String server_url = config.getString("conf_server_url",
                                "http://qrhc.bpmtech.com");
                String server_port = config.getString("conf_server_port", "80");
                String server_db_name = config.getString("conf_server_db_name",
                                "cards030913");
                String user_name = config.getString("conf_user_name", "admin");
                String user_passwd = config.getString("conf_user_passwd", "admin");

                Integer user_id = this._auth_openerp(server_url, server_port,
                                server_db_name, user_name, user_passwd);
                if (user_id > 0) {
                        //Se crea un objeto de tipo XMLRPCClient para consultar OpenERP via RPC.
                        XMLRPCClient cliente_objeto = new XMLRPCClient(server_url + ":"
                                        + server_port + "/xmlrpc/object");
                        try {                        
                                String[][] filtro = { { "resource_id.name", "=", card_id }, };
                                /*
                                 * El metodo call permite ejecutar funciones remotas (RPC)
                                 * Los parametros de esta funcion son los necesarios del metodo remoto (ver doc de OpenERP)
                                 */
                                Object[] encontrados = (Object[]) cliente_objeto.call(
                                                "execute", server_db_name, user_id, user_passwd,
                                                "cards.cards", "search", filtro);
                                if (encontrados.length == 1) {
                                        String encontrado1 = encontrados[0].toString();
                                        String[] ids = { encontrado1 };
                                        String[] campos = {"state"};
                                        Object[] objeto = (Object[]) cliente_objeto.call("execute",
                                                        server_db_name, user_id, user_passwd,
                                                        "cards.cards", "read", ids, campos);
                                        if (objeto.length == 1) {
                                                try {
                                                        HashMap<String, Object> objeto_mapa = (HashMap<String, Object>) objeto[0];
                                                        String card_state = objeto_mapa.get("state").toString();
                                                        if (card_state.length() > 0) {
                                                                return card_state;
                                                        } else {
                                                                throw new Exception("No definido");
                                                        }
                                                } catch (Exception ex) {
                                                        
                                                }
                                        } else {
                                                throw new Exception("Not valid Card");
                                        }

                                } else {
                                        throw new Exception("Tarjeta no encontrada en el sistema");
                                }
                        } catch (XMLRPCException ex) {
                                throw new Exception("Error en la conexion, revise la configuracion");
                        } catch (Exception ex) {
                                throw new Exception("Ha ocurrido un error desconocido: " + ex.getMessage());
                        }
                } else if (user_id == -1) {
                        throw new Exception("Error en la conexion con el servidor por favor revise la configuracion");
                } else {
                        throw new Exception("Usuario o clave incorrecta, por favor revise la configuracion");
                }                
                return "";
        }
        
        private Integer _auth_openerp(String server_url, String server_port,
                        String server_db_name, String user_name, String passwd) {
                try {
                        XMLRPCClient cliente_login = new XMLRPCClient(server_url + ":"
                                        + server_port + "/xmlrpc/common");
                        Integer user_id = (Integer) cliente_login.call("login",
                                        server_db_name, user_name, passwd);
                        if (user_id > 0)
                                return user_id;
                } catch (XMLRPCException ex) {                        
                        return -1;
                } catch (Exception ex) {
                        return 0;
                }
                return 0;
        }
        
        
        
        public void envioDatos(View view){
        
        	
        String cardId = card_data.get("card_id");
        
        Date dt = new Date();


        int hours = dt.getHours();
        int minutes = dt.getMinutes();
        int seconds = dt.getSeconds();
        hora = hours+":"+minutes+":"+seconds;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        fecha = df.format(c.getTime());
        
        gps = lati + longi;
        

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("cardId", cardId));
        postParameters.add(new BasicNameValuePair("gps", lati));
        postParameters.add(new BasicNameValuePair("fecha", fecha));
    

        String response = null;
        try {
       response = CustomHttpClient.executeHttpPost("http://server6.itesecc.com/readqr.php", postParameters);
        String res=response.toString();
        res= res.replaceAll("\\s+",""); 
        
        if(res.equals("1"))  {
        	Toast.makeText(context,"Datos enviados." + res, Toast.LENGTH_LONG).show();
        }
        else{
        	Toast.makeText(context,"Error en envio de datos.",Toast.LENGTH_LONG).show();
        }
        
            
        
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        }
        

        private void comenzarLocalizacion()
        {
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mostrarPosiciond(loc, vali);

        locListener = new LocationListener() {
        	public void onLocationChanged(Location locations) {
        		vali = "1";
        		mostrarPosiciond(locations, vali);
        	}
        	public void onProviderDisabled(String provider){
        	//	lblEstado.setText("Provider OFF");
        	}
        	public void onProviderEnabled(String provider){
        		//lblEstado.setText("Provider ON ");
        	}
        	public void onStatusChanged(String provider, int status, Bundle extras){
        		Log.i("", "Provider Status: " + status);
        		//lblEstado.setText("Provider Status: " + status);
        	}
        };
        //Se ejecuta autmaticamente para obtener actualizaciones de GPS
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
        	//locManager.removeUpdates(locListener);
          	
        } 

        private void mostrarPosiciond(Location locd, String va) { 
        if(locd != null)
        	{
        	if (va == "1"){
        		//invoca la actualizacion automatica del GPS, pero sin enviar datos a BD
        		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
        	}
        	else{
        		lati = String.valueOf(locd.getLatitude());
        		longi = String.valueOf(locd.getLongitude());
        		Toast.makeText(context,"Entra al asignador.",Toast.LENGTH_LONG).show();
        		//setLat(latid);
        		//Toast.makeText(this, " "+latid, Toast.LENGTH_LONG).show();
        		//envioDatos();
        	}
        	//Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
        }
        else
        {
        	Toast.makeText(context,"Era Null.",Toast.LENGTH_LONG).show();	
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


