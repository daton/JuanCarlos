 package com.unitec.localizamigos


import kotlinx.android.synthetic.main.activity_mapita.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import org.json.JSONObject


import java.lang.ref.WeakReference
import java.time.LocalDate
import java.util.*

 class MapitaActivity : AppCompatActivity() , OnMapReadyCallback, PermissionsListener {
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap

    //Los siguientes atributos son para ajustar el rango mni
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private var locationEngine: LocationEngine? = null



    private val callback: LocationChangeListeningActivityLocationCallback =
            LocationChangeListeningActivityLocationCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Mapbox.getInstance(this, getString(R.string.miToken))


        setContentView(R.layout.activity_mapita)

        mapita.onCreate(savedInstanceState)
        mapita.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) {

            // Habilitamos la localizacion
            enableLocationComponent(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Checams si los permisos de localizacion son otorgados
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Personalizacion de opciones de localizacion
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this, R.color.design_default_color_on_secondary))
                    .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                    this,
                    loadedMapStyle
            )
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Obtenemos una instancia del a componente de localizacion
            mapboxMap.locationComponent.apply {

                // Activamos la localizacin con opciones
                activateLocationComponent(locationComponentActivationOptions)

                // Habilitamos si la localizacionesta en true
                isLocationComponentEnabled = true

                // ponemos el tracking activado
                cameraMode = CameraMode.TRACKING

                // ajustamos la brujula como visible
                renderMode = RenderMode.COMPASS

                //El nuevo
                initLocationEngine();
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine?.requestLocationUpdates(request, callback, mainLooper)
        locationEngine?.getLastLocation(callback)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, "Necesitas tu localizacion", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, "No conediste el permiso de localozacion", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapita.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapita.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapita.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapita.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapita.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapita.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapita.onLowMemory()
    }


    inner  class LocationChangeListeningActivityLocationCallback internal constructor(activity: MapitaActivity?):
            LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<MapitaActivity>


        override fun onSuccess(result: LocationEngineResult){
            val activity:MapitaActivity=activityWeakReference.get()!!
            if(activity!=null){
                val location=result.lastLocation?:return


                Toast.makeText(
                        activity,
                        "${result.lastLocation!!.latitude.toString()},${result.lastLocation!!.longitude.toString()},${result.lastLocation!!.altitude.toString()}",
                        Toast.LENGTH_SHORT
                ).show()

              //  Globales.lat=result.lastLocation!!.latitude
              //  Globales.lng=result.lastLocation!!.longitude
                //En la siguiente clase vamos a poner en eta seccion una corutina con la cual
                //estaremos enviando cada segundo o cuando tenga conexion
                //la ultima localizacion actualziada.
/*
INICIA REQUEST
 */

                var usuariojson= JSONObject()
                var loca=JSONObject()
                loca.put("lat",result.lastLocation!!.latitude.toString())
                loca.put("lon", result.lastLocation!!.longitude.toString())
                loca.put("fecha", Date().toString())

                usuariojson.put("email","rapidclimate@gmail.com")
                usuariojson.put("localizacion", loca)


                //Punto 4. Generar el objeto de tipo Request donde se enviar al back-end nuestro usuario
                // var url="https://benesuela.herokuapp.com/api/usuario"
                var url="http://192.168.100.101:8080/api/usuario"
                val jsonObjectRequest = JsonObjectRequest(Request.Method.PUT, url, usuariojson,
                        Response.Listener { response ->
                            //Ponemo la notificacion del back end en una objeto de tipo Toast
                            Toast.makeText(applicationContext,  response.get("mensaje").toString(), Toast.LENGTH_LONG).show()
                        },
                        Response.ErrorListener { error ->
                            // TODO: Handle error
                            Toast.makeText(applicationContext,  "Hubo un error, ${error}", Toast.LENGTH_LONG).show()

                        }
                )

                // Acceso al request por medio de una clase Singleton
                MiSingleton.getInstance(applicationContext).addToRequestQueue(jsonObjectRequest)

/*
TERMINA REQUEST
 */


//PassthenewlocationtotheMapsSDK'sLocationComponent
                if(activity.mapboxMap!=null&&result.lastLocation!=null){
                    activity.mapboxMap.getLocationComponent()
                            .forceLocationUpdate(result.lastLocation)
                }
            }
        }

        /**
         *TheLocationEngineCallbackinterface'smethodwhichfireswhenthedevice'slocationcan'tbecaptured
         *
         *@paramexceptiontheexceptionmessage
         */
        override fun onFailure(exception: Exception){
            var activity :MapitaActivity=activityWeakReference.get()!!
            if(activity!=null){
                Toast.makeText(
                        activity, exception.localizedMessage,
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

        init{
            activityWeakReference=WeakReference(activity)
        }
    }

    //Esta es la clase tipo singleton que nos ayuda  a generar la conexion a internet como una
//tarea o Thread separado.
    class MiSingleton constructor(context: Context) {
        companion object {
            @Volatile
            private var INSTANCE: MiSingleton? = null
            fun getInstance(context: Context) =
                    INSTANCE ?: synchronized(this) {
                        INSTANCE ?: MiSingleton(context).also {
                            INSTANCE = it
                        }
                    }
        }

        //Para el caso de cargar un objeto como una imagen.
        val imageLoader: ImageLoader by lazy {
            ImageLoader(requestQueue,
                    object : ImageLoader.ImageCache {
                        private val cache = LruCache<String, Bitmap>(20)
                        override fun getBitmap(url: String): Bitmap {
                            return cache.get(url)
                        }
                        override fun putBitmap(url: String, bitmap: Bitmap) {
                            cache.put(url, bitmap)
                        }
                    })
        }
        val requestQueue: RequestQueue by lazy {
            // applicationContext es para evitar fuga de memoria

            Volley.newRequestQueue(context.applicationContext)
        }
        fun <T> addToRequestQueue(req: Request<T>) {
            requestQueue.add(req)
        }
    }

}
