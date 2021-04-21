package com.unitec.localizamigos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.unitec.localizamigos.modelo.ServicioUsuario
import com.unitec.localizamigos.modelo.Usuario
import kotlinx.android.synthetic.main.activity_recycler_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class ActivityRecyclerView : AppCompatActivity() {


    var usuarios=ArrayList<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        //Invocamos el metodo todos
        todos()


    }

    fun todos() {

        GlobalScope.launch(Dispatchers.IO){


            var retrofit = Retrofit.Builder()
                .baseUrl("https://benesuela.herokuapp.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build()

            var servicioUsuario = retrofit.create(ServicioUsuario::class.java)
            var enviarUsuarios = servicioUsuario.buscarTodos()

            usuarios = enviarUsuarios.execute().body()!!


            launch(Dispatchers.Main) {
               txtEncontrados.text="Usuarios: ${usuarios.size} el primer es ${usuarios.get(0).nombre}"
            }



        }


    }
}