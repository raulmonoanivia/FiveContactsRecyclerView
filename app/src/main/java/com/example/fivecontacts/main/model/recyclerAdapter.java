package com.example.fivecontacts.main.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fivecontacts.R;

import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.MyViewHolder> {
    private ArrayList<Contato> contatos;

    public recyclerAdapter(ArrayList<Contato> contatos){

        this.contatos = contatos;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView tvNome, tvAbrev;
        private ImageView tvFoto;

        public MyViewHolder(final View view){
            super(view);
            tvNome = view.findViewById(R.id.userTitle);


        }
    }

    @NonNull
    @Override
    public recyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerAdapter.MyViewHolder holder, int position) {
        String name = contatos.get(position).getNome();


    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }
}
