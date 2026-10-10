package com.Jair.practica

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val listoa =findViewById<ListView>(R.id.idListview)
        val boton= findViewById<Button>(R.id.idbuton)
        //arreglo de los datos
        val arregloPais = arrayOf(
            "Perú","Paraguay","Bolivia","Bresil","Panama","Argentina"
        )
        val arrgloCapital= arrayOf(
                "Lima","Asunción", "Sucre", "Brasilia", "Ciudad de Panamá", "Buenos Aires"
                )

        //crear o personalisar adaptador
        val adaptador = ArrayAdapter(this,android.R.layout.activity_list_ite, arregloPais)
        val numero= -1

        //lista de los adapter en paises
        listoa.onItemClickListener{-,-, numero,->}

        }

    }



