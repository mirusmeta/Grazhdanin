package ru.southcode

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await

class CustomAdapter(private val context: Context, private val dataList: List<CustomModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.textViewName.text = currentItem.name
        holder.textViewDescription.text = currentItem.description
        holder.iddoc.text = currentItem.iddoc
        if(helloactivity.isAdmin){
            holder.itemView.setOnClickListener {
                val intent = Intent(context, RaitingAdmin::class.java)
                intent.putExtra("id", currentItem.iddoc)
                context.startActivity(intent)
            }
        }
        else{
            holder.itemView.setOnClickListener {
                val intent = Intent(context, raiting::class.java)
                intent.putExtra("id", currentItem.iddoc)
                context.startActivity(intent)
            }
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child(currentItem.imageUrl)

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Успешно получили ссылку, загружаем изображение с использованием Picasso
            Picasso.get().load(uri).into(holder.imagev)
        }.addOnFailureListener { exception ->
            // Обработка ошибки
            Log.e("FirestoreImageLoadError", "Failed to load image: ${exception.message}")
        }
    }


    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.name)
        val textViewDescription: TextView = itemView.findViewById(R.id.place)
        val iddoc: TextView = itemView.findViewById(R.id.iddoc)
        val imagev: ImageView = itemView.findViewById(R.id.imagev)
    }
}
