package pt.ipt.dam2025.phototravel.fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.ipt.dam2025.phototravel.R
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapaFragmento.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapaFragmento : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var vistaMapa: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        MapLibre.getInstance(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    /**
     * Criar o mapa
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vistaMapa = view.findViewById<MapView>(R.id.mapa)

        vistaMapa.onCreate(savedInstanceState)

        vistaMapa.getMapAsync {

            map -> map.setStyle("https://api.maptiler.com/maps/aquarelle/style.json?key=WFHHB4Zg3NcUnxvMy6uZ") {
                map.cameraPosition = CameraPosition.Builder().
                        target(LatLng(48.8566, 2.3522)).
                        zoom(10.0).
                        build()
            }

        }
    }


    /**
     * Ciclo de vida do mapa - Início
     */
    override fun onStart() {
        super.onStart()
        vistaMapa.onStart()
    }
    /**
     * Ciclo de vida do mapa - Resumo
     */
    override fun onResume() {
        super.onResume()
        vistaMapa.onResume()
    }
    /**
     * Ciclo de vida do mapa - Pausa
     */
    override fun onPause() {
        super.onPause()
        vistaMapa.onPause()
    }
    /**
     * Ciclo de vida do mapa - Parar
     */
    override fun onStop() {
        super.onStop()
        vistaMapa.onStop()
    }
    /**
     * Ciclo de vida do mapa - Memória
     */
    override fun onLowMemory() {
        super.onLowMemory()
        vistaMapa.onLowMemory()
    }
    /**
     * Ciclo de vida do mapa - Destruir
     */
    override fun onDestroyView() {
        super.onDestroyView()
        vistaMapa.onDestroy()
    }
    /**
     * Ciclo de vida do mapa - Guardar estado
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vistaMapa.onSaveInstanceState(outState)
    }




/**
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapaFraguemento.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapaFraguemento().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    **/
}