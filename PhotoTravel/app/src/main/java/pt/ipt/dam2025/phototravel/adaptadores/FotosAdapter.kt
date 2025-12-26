package pt.ipt.dam2025.phototravel.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2025.phototravel.R
import pt.ipt.dam2025.phototravel.modelos.FotoDados

class FotosAdapter(private val listaFotos: List<FotoDados>) :
    RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {
        class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imagem: ImageView = view.findViewById(R.id.imgFotoSimples)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_foto_colecoes, parent, false)
            return FotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val foto = listaFotos[position]
            try {
                holder.imagem.setImageURI(foto.uriString.toUri())
            } catch (e: Exception) {
                // Ignorar erro se a imagem não carregar
            }
        }

        override fun getItemCount() = listaFotos.size
    }