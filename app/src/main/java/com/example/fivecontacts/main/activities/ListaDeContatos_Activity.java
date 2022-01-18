package com.example.fivecontacts.main.activities;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fivecontacts.R;
import com.example.fivecontacts.main.model.Contato;
import com.example.fivecontacts.main.model.User;
import com.example.fivecontacts.main.model.recyclerAdapter;
import com.example.fivecontacts.main.utils.UIEducacionalPermissao;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ListaDeContatos_Activity extends AppCompatActivity implements UIEducacionalPermissao.NoticeDialogListener, BottomNavigationView.OnNavigationItemSelectedListener {

    ListView lv;
    BottomNavigationView bnv;
    User user;

    TextView textLigar;
    TextView delContato;

    boolean holding = false;
    Uri uriAtual;
    boolean callDenied = false;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_contatos);

        bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(this);
        bnv.setSelectedItemId(R.id.anvLigar);
        lv = findViewById(R.id.listView1);
        textLigar = findViewById(R.id.tvLigar);
        delContato = findViewById(R.id.tvDelete);

        recyclerView = findViewById(R.id.recyclerContatos);

        // Dados da Intent anterior
        Intent quemChamou = this.getIntent();
        if (quemChamou != null) {
            Bundle params = quemChamou.getExtras();
            if (params != null) {
                // Recuperando o Usuário
                user = (User) params.getSerializable("usuario");
                if (user != null) {
                    setTitle("Contatos de Emergência de " + user.getNome());
                    // Montando a ListView
                    preencherListViewImagens(user);
                }
            }
        }

    }


    protected void atualizarListaDeContatos(User user){
        SharedPreferences recuperarContatos = getSharedPreferences("contatos", Activity.MODE_PRIVATE);



        int num = recuperarContatos.getInt("numContatos", 0);
        ArrayList<Contato> contatos = new ArrayList<Contato>();

        Contato contato;

        for (int i = 1; i <= num; i++) {
            String objSelelected = recuperarContatos.getString("contato" + i, "");
            if (objSelelected.compareTo("") != 0) {
                try {
                    ByteArrayInputStream bis =
                            new ByteArrayInputStream(objSelelected.getBytes(StandardCharsets.ISO_8859_1.name()));
                    ObjectInputStream oos = new ObjectInputStream(bis);
                    contato = (Contato) oos.readObject();

                    if (contato != null) {
                        contatos.add(contato);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Log.v("PDMMat","Contatos: " + contatos.size());
        user.setContatos(contatos);
    }

    protected void deletarContatoDaLista(Contato c){
        SharedPreferences resgatarContatosAtuais = getSharedPreferences("contatos", Activity.MODE_PRIVATE);

        int numero = resgatarContatosAtuais.getInt("numContatos", 0);

        Contato contato;
        int toBeDeleted = 0;


        for (int i = 1; i <= numero; i++) {
            String objSel = resgatarContatosAtuais.getString("contato" + i, "");
            if (objSel.compareTo("") != 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(objSel.getBytes(StandardCharsets.ISO_8859_1.name()));
                    ObjectInputStream oos = new ObjectInputStream(bis);
                    contato = (Contato) oos.readObject();

                    // Checagem do contato
                    if (contato != null && c.getNumero().equals(contato.getNumero())) {
                        toBeDeleted = i;
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Remover contato do SharedPreferences
        SharedPreferences.Editor editor = resgatarContatosAtuais.edit();
        editor.remove("contato" + toBeDeleted);
        editor.commit(); // Salvar no disco

        Toast.makeText(this, "Contato deletado!", Toast.LENGTH_LONG).show();

        user = atualizarUser();
        atualizarListaDeContatos(user);
        preencherListViewImagens(user);
    }

    // Função para acionar ou não a função de deletar contato
    protected boolean deleteDialog(final Contato c){
        // Ações de um possível alerta
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        deletarContatoDaLista(c);
                        holding = false;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        holding = false;
                        break;
                    default:
                        holding = false;
                        break;
                }
            }
        };

        // Confirmação de exclusão
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Você realmente quer deletar esse contato?")
                .setPositiveButton("Sim", dialogClickListener)
                .setNegativeButton("Não", dialogClickListener)
                .setCancelable(false)
                .show();

        return false;
    }

    // Preencher a página com contatos
    protected void preencherListViewImagens(User user){

        final ArrayList<Contato> contatos = user.getContatos();
        Collections.sort(contatos);


        if (contatos != null) {
            textLigar.setText("Toque para ligar");
            delContato.setVisibility(View.VISIBLE);

            // Checar se há contatos para mudar os textos exibidos
            if(contatos.isEmpty()){
                textLigar.setText("Sem contatos");
                delContato.setVisibility(View.INVISIBLE);
            }

            String[] contatosNomes, contatosAbrevs;
            contatosNomes = new String[contatos.size()];
            contatosAbrevs = new String[contatos.size()];

            // Coletando nomes e abrevs dos contatos
            for (int j = 0; j < contatos.size(); j++) {
                contatosAbrevs[j] =contatos.get(j).getNome().substring(0, 1);
                contatosNomes[j] =contatos.get(j).getNome();
            }

            // Criação do Array
            final ArrayList<Map<String,Object>> itemDataList = new ArrayList<Map<String,Object>>();;

            for(int i = 0; i < contatos.size(); i++) {
                Map<String,Object> listItemMap = new HashMap<String,Object>();
                listItemMap.put("imageId", R.drawable.ic_action_ligar_list);
                listItemMap.put("contato", contatosNomes[i]);
                listItemMap.put("abreviation", contatosAbrevs[i]);
                itemDataList.add(listItemMap);
            }
            final SimpleAdapter simpleAdapter = new SimpleAdapter(this,itemDataList,R.layout.cardview,
                    new String[]{"imageId","contato","abreviation"},new int[]{R.id.userImage, R.id.userTitle,R.id.userAbrev});


            //Hook Adapter
            recyclerAdapter adapter = new recyclerAdapter(contatos);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            lv.setAdapter(simpleAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                    Uri uri = Uri.parse(contatos.get(i).getNumero());
                    uriAtual = uri;

                    // Checa se tem permissão para realizar a ligação e se o botão não foi segurado
                    if (checarPermissaoPhone_SMD() && !holding) {
                        Intent itLigar = new Intent(Intent.ACTION_CALL, uri);
                        startActivity(itLigar);
                    }
                }
            });

            // Função de deletar contato
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    holding = true; // Impede a ligação por tocar enquanto segura
                    Contato contato = contatos.get(position);
                    deleteDialog(contato);

                    return false;
                }
            });
        } else {
            textLigar.setText("Sem contatos");
            delContato.setVisibility(View.INVISIBLE);
        }
    }

    // Checar permissão de ligação
    protected boolean checarPermissaoPhone_SMD(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED){

            Log.v ("Permissao","Tenho permissão");

            return true;

        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)){

                Log.v ("Permissao","Primeira Vez");

                String mensagem = "Precisamos da sua permissão para ligar automaticamente!\n\nSe a permissão for negada, apenas o discador será iniciado.";
                String titulo = "Permissão para ligações";
                int codigo = 1;
                UIEducacionalPermissao mensagemPermissao = new UIEducacionalPermissao(mensagem, titulo, codigo);

                mensagemPermissao.onAttach ((Context)this);
                mensagemPermissao.show(getSupportFragmentManager(), "primeiravez2");

            } else if (!callDenied) {
                String mensagem = "Precisamos da sua permissão para ligar automaticamente!\n\nSe a permissão for negada, apenas o discador será iniciado.";
                String titulo = "Permissão para ligações";
                int codigo = 1;

                UIEducacionalPermissao mensagemPermissao = new UIEducacionalPermissao(mensagem, titulo, codigo);
                mensagemPermissao.onAttach ((Context)this);
                mensagemPermissao.show(getSupportFragmentManager(), "segundavez2");
                Log.v ("Permissao","Outra Vez");
            } else {
                callDeniedExplanation();
            }
        }

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 2222:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Obrigado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permissão Negada :(", Toast.LENGTH_LONG).show();
                    callDeniedExplanation();
                }
                break;
        }
    }

    // Mostrar mensagem de explicação da necessidade caso a permissão seja negada
    public void callDeniedExplanation() {
        String mensagem = "Essa mensagem sempre aparecerá enquanto o aplicativo não tiver sua permissão!\n\nPrecisamos da sua permissão para fazer as ligações\n\nCaso tenha marcado a opção de não perguntar novamente, só será possível alterar as permissões manualmente, na tela de informações do aplicativo, ou reinstalando o aplicativo";
        String titulo = "Porque precisamos da permissão?";
        int codigo = 2;

        UIEducacionalPermissao mensagemPermisso = new UIEducacionalPermissao(mensagem, titulo, codigo);
        mensagemPermisso.onAttach((Context)this);
        mensagemPermisso.show(getSupportFragmentManager(), "segundavez");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Checagem de o Item selecionado é o do perfil
        if (item.getItemId() == R.id.anvPerfil) {
            //Abertura da Tela MudarDadosUsario
            Intent intent = new Intent(this, PerfilUsuario_Activity.class);
            intent.putExtra("usuario", user);
            startActivityForResult(intent, 1111);

        }
        // Checagem de o Item selecionado é o de mudança de contatos
        if (item.getItemId() == R.id.anvMudar) {
            //Abertura da Tela Adicionar Contatos
            Intent intent = new Intent(this, AlterarContatos_Activity.class);
            intent.putExtra("usuario", user);
            startActivityForResult(intent, 1112);

        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Caso seja um Voltar ou Sucesso selecionar o item Ligar

        if (requestCode == 1111) { // Retorno de Mudar Perfil
            bnv.setSelectedItemId(R.id.anvLigar);
            user = atualizarUser();
            setTitle("Contatos de Emergência de "+ user.getNome());
            atualizarListaDeContatos(user); // Função que atualiza a lista de contatos
            preencherListViewImagens(user); // Função que monta o ListView
        }

        if (requestCode == 1112) { // Retorno de Mudar Contatos
            bnv.setSelectedItemId(R.id.anvLigar);
            atualizarListaDeContatos(user); // Função que atualiza a lista de contatos
            preencherListViewImagens(user); // Função que monta o ListView
        }
    }

    // Atualizar User no SharedPreferences
    private User atualizarUser() {
        User user = null;
        SharedPreferences temUser= getSharedPreferences("usuarioPadrao", Activity.MODE_PRIVATE);
        String loginSalvo = temUser.getString("login","");
        String senhaSalva = temUser.getString("senha","");
        String nomeSalvo = temUser.getString("nome","");
        String emailSalvo = temUser.getString("email","");
        boolean manterLogado = temUser.getBoolean("manterLogado",false);

        user = new User(nomeSalvo,loginSalvo,senhaSalva,emailSalvo,manterLogado);
        return user;
    }

    @Override
    public void onDialogPositiveClick(int codigo) {

        if (codigo == 1){
            String[] permissions = {Manifest.permission.CALL_PHONE};
            requestPermissions(permissions, 2222);
        }
        if (codigo == 2){
            callDenied = true;
            Intent itLigar = new Intent(Intent.ACTION_DIAL, uriAtual);
            startActivity(itLigar);
        }
    }
}


