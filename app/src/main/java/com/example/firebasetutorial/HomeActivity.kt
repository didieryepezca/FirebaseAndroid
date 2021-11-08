package com.example.firebasetutorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.RuntimeException

enum class ProveedorTipo {
    BASICO,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() //Instancia que conecta a la BD remota de Firebase.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //creamos funcion que se ejecutara con la carga. los bundles son para obtener los datos que vienen de la otra pantalla.
        var bundle = intent.extras
        var email:String? = bundle?.getString("email")
        var provider:String? = bundle?.getString("provider")
        setup(email?:"",provider?:"")

        //Guardar el usuario autenticado a nivel de app.
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply() //aplicamos los cambios a los strings
    }

    private fun setup(email:String, provider: String){

        title = "Inicio"

        emailTextView.text = email
        providerTextView.text = provider

        logoutButton.setOnClickListener(){
            //en el momento que presionamos CERRAR SESION se eliminan los datos temporales del usuario
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit() //obtenemos el string creado en "strings.xml"
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()// para volver a la pantalla anterior
        }


        //Probando Crashlitycs
        errorButton.setOnClickListener(){

            FirebaseCrashlytics.getInstance().setUserId(email)
            FirebaseCrashlytics.getInstance().setCustomKey("proveedor", provider) //enviando distintos tipos de datos a CrashLitycs

            throw RuntimeException("Error: Probando Crashlitycs")
        }


        //Accedemos a Firestore.
        saveButton.setOnClickListener(){
            db.collection("users").document(email).set( //la clave primaria seria lo que va dentro de document()
                hashMapOf("proveedor" to provider,
                "address" to addressTextView.text.toString(),
                "phone" to phoneTextView.text.toString())
            )
        }

        getButton.setOnClickListener(){
            db.collection("users").document(email).get().addOnSuccessListener {
                addressTextView.setText(it.get("address") as String?)
                phoneTextView.setText(it.get("phone") as String?)
            }
        }

        deleteButton.setOnClickListener(){

            db.collection("users").document(email).delete()

        }


    } // Fin de nuestra funcion Setup



}