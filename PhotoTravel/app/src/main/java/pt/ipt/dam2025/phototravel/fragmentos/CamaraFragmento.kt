package pt.ipt.dam2025.phototravel.fragmentos


import android.Manifest
import android.content.pm.PackageManager
import android.content.ContentValues
import android.location.Location
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import pt.ipt.dam2025.phototravel.modelos.FotoDados
import pt.ipt.dam2025.phototravel.viewmodel.PartilhaDadosViewModel
import pt.ipt.dam2025.phototravel.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class CamaraFragmento : Fragment() {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val viewModel: PartilhaDadosViewModel by activityViewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var ultimaLocal: Location? = null
    
    // Variável para evitar o loop infinito do diálogo de GPS
    private var gpsDialogJaSolicitado = false

    /**
     * Callback para receber as atualizações de localização
     */
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            ultimaLocal = result.lastLocation
            Log.d("GPS", "Localização atualizada")
        }
    }

    /**
     * Launcher para ativar o GPS
     */
    private val gpsAtivo = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // O utilizador aceitou ligar o GPS! Iniciamos o rastreio.
            rastrearGPS()
        } else {
            // O utilizador recusou. A flag impede que o onResume peça novamente.
            Toast.makeText(requireContext(), "GPS necessário para guardar localização nas fotos.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launcher para solicitar permissões
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (cameraGranted) {
            iniciarCamara()
        } else {
            Toast.makeText(requireContext(), "É necessário dar permissão à câmara para tirar fotos.", Toast.LENGTH_LONG).show()
        }

        if (locationGranted) {
            verificarGpsRastrear()
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camara, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // listener do botão de tirar foto
        view.findViewById<ImageButton>(R.id.image_capture_button).setOnClickListener { tirarFoto() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Função para verificar as permissões e caso fornecidas iniciar a camara
     */
    private fun verificarPermissaoEIniciarCamara() {
        val cameraP = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val locationP = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (cameraP) {
            iniciarCamara()
        }
        
        if (locationP) {
            verificarGpsRastrear()
        }

        if (!cameraP || !locationP) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }
    }


    /**
     *Função para iniciar a câmara
     */
    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // pré-visualização
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view?.findViewById<PreviewView>(R.id.viewFinder)?.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            // Seleciona a câmara traseira (default)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincula tudo antes de vincular novamente
                cameraProvider.unbindAll()

                // Vincula ao viewLifecycleOwner para garantir que segue o ciclo de vida da UI
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("CamaraFragmento", "Falha ao vincular casos de uso", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     *  Função para desativar a câmara,
     *  assim quando se muda de fragmento a camara é desativada
     *
     */
    private fun desativarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Função para tirar uma foto
     */
    private fun tirarFoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        val dataDia = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoTravel")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CamaraFragmento", "Erro ao tirar a foto: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Erro ao guardar a foto.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri ?: return

                    if (ultimaLocal != null) {
                        val novaFoto = FotoDados(
                            uriString = uri.toString(),
                            titulo = name,
                            data = dataDia,
                            latitude = ultimaLocal?.latitude ?: 0.0 ,
                            longitude = ultimaLocal?.longitude ?: 0.0
                        )
                        viewModel.adicionarFotos(novaFoto)
                        Toast.makeText(requireContext(), "Foto guardada com localização!", Toast.LENGTH_SHORT).show()
                    } else {
                        val novaFoto = FotoDados(uri.toString(), name, dataDia, null, null)
                        viewModel.adicionarFotos(novaFoto)
                        Toast.makeText(requireContext(), "Foto guardada (sem GPS).", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    /**
     * Função para rastrear a localização
     */
    private fun rastrearGPS(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }

        try{
            //obter localização em cache caso exista para ser mais rápido
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if(location != null){
                    ultimaLocal = location
                }
            }

            //fazer pedidos constantes de atualização da localização
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
            ).apply {
                setMinUpdateDistanceMeters(5.0f)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        }catch (e: Exception){
            Log.e("GPS", "Erro ao iniciar rastreio: ${e.message}")
        }
    }


    /**
     * Função para verificar se o gps está ativo no sistema
     */
    private fun verificarGpsRastrear(){
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val cliente = LocationServices.getSettingsClient(requireContext())
        val task = cliente.checkLocationSettings(builder.build())

        task.addOnSuccessListener { 
            rastrearGPS() 
        }

        task.addOnFailureListener { exception ->
            // Só pedimos para ativar se ainda não tivermos perguntado nesta "sessão"
            if (exception is ResolvableApiException && !gpsDialogJaSolicitado) {
                try {
                    gpsDialogJaSolicitado = true
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    gpsAtivo.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("GPS", "Erro ao tentar mostrar o diálogo de GPS", e)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        verificarPermissaoEIniciarCamara()
    }

    override fun onPause() {
        super.onPause()
        desativarCamara()
        // Parar atualizações de localização para poupar bateria
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
