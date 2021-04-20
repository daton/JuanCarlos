package com.unitec.localizamigos

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.unitec.localizamigos.modelo.Usuario
import org.json.JSONObject

import kotlinx.android.synthetic.main.activity_registro.*



class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

     //Manejamos el evento del boton para pedir los valores de las componentes de
        //nuestro formulario
         registrar.setOnClickListener {
//Punto 2:
             var usuario= Usuario()
             //Este usuario que se lla usuario, lo llenamos con los valores de los campos de texto del formulario:
            usuario.email=  txtEmail.text.toString()
            usuario.nickname=  txtNickname.text.toString()
            usuario.nombre= txtNombre.text.toString()

             //El siguiente paso es este objeto que acabamos de crear(Usuario) lo tenemos que enviar a un servidor
             //eexterno par que pueda se gurdado y registrado, asi coomo se registran en cualquier red social.
             //Para este paso necesitamos enviar est informacion a un servidor  y el mecanismo de envio es una arquitectura
             //muy particular que se denomina Arquitectura estilo REST
             //En Android existe una tecnologia muy particular que nos va a ayudar para poder enviar nuestor objeto
             //de registro a el back-end. Esta tecnologia se conoce como RETROFIT
        //Punto 3: Generar el objeto JSON de los datos de unto 2.
         var usuariojson=JSONObject()
             usuariojson.put("email", usuario.email)
             usuariojson.put("nickname", usuario.nickname)
             usuariojson.put("nombre",usuario.nombre)



          //Punto 4. Generar el objeto de tipo Request donde se enviar al back-end nuestro usuario

            var url="https://benesuela.herokuapp.com/api/usuario"
             val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, usuariojson,
                     Response.Listener { response ->
                         //Ponemo la notificacion del back end en una objeto de tipo Toast

                      //AQUI VAMOS A GUARDAR EL OBJETO EN SharedPreferences

     val preferencias=applicationContext?.getSharedPreferences("AMIGOS", Context.MODE_PRIVATE)?:return@Listener
                      //Con notacion funcional con lambdas mas moderno y mas seguro al nullpointer exception
                         with(preferencias.edit()){
                             putString("nombre", usuario.nombre).commit()
                             putString("email", usuario.email).commit()
                            // putFloat("edad", 19.8f).commit()
                             deleteFile("AMIGOS")
                         }

                        //El equivalente de arriba pero orientado a objeto
                        // preferencias?.edit()?.putString("nombre", usuario.nombre)?.commit()
                         //Puedes guardar cualquier otro tipo de variable no necesriamente string
                       //  preferencias?.edit()?.putFloat("edad", 19.5f)

                         Toast.makeText(this,  response.get("mensaje").toString(), Toast.LENGTH_LONG).show()
                     },
                     Response.ErrorListener { error ->
                         // TODO: Handle error
                         Toast.makeText(this,  "Hubo un error, ${error}", Toast.LENGTH_LONG).show()

                     }
             )

             // Acceso al request por medio de una clase Singleton
             MiSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)





         }

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

