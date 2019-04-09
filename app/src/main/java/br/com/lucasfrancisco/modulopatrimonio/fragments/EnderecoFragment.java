package br.com.lucasfrancisco.modulopatrimonio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import br.com.lucasfrancisco.modulopatrimonio.R;
import br.com.lucasfrancisco.modulopatrimonio.activities.EnderecoActivity;
import br.com.lucasfrancisco.modulopatrimonio.activities.NovoEnderecoActivity;
import br.com.lucasfrancisco.modulopatrimonio.adapters.EnderecoAdapter;
import br.com.lucasfrancisco.modulopatrimonio.interfaces.RecyclerViewClickListener;
import br.com.lucasfrancisco.modulopatrimonio.models.Endereco;

public class EnderecoFragment extends Fragment {
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestore.collection("Enderecos");

    private EnderecoAdapter enderecoAdapter;

    private FloatingActionMenu famEndereco;
    private FloatingActionButton fabNovo, fabXls;
    private RecyclerView rcyEnderecos;

    private ArrayList<String> listEnderecos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_endereco, container, false);
        Query query = collectionReference.orderBy("cep", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Endereco> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Endereco>().setQuery(query, Endereco.class).build();
        enderecoAdapter = new EnderecoAdapter(firestoreRecyclerOptions);

        rcyEnderecos = (RecyclerView) view.findViewById(R.id.rcyEnderecos);
        rcyEnderecos.setHasFixedSize(true);
        rcyEnderecos.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcyEnderecos.setAdapter(enderecoAdapter);

        famEndereco = (FloatingActionMenu) view.findViewById(R.id.famEndereco);
        fabNovo = (FloatingActionButton) view.findViewById(R.id.fabNovo);
        fabXls = (FloatingActionButton) view.findViewById(R.id.fabXls);

        getListEnderecos();
        // Métodos para eventos
        //getRcyEnderecos();
        getRecyclerViewClickListener();
        getFabXls();
        getFabNovo();

        return view;
    }

    private void getRcyEnderecos() {
        rcyEnderecos.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeFloatingActionMenu();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        enderecoAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        enderecoAdapter.stopListening();
    }

    public void closeFloatingActionMenu() {
        if (famEndereco.isOpened()) {
            famEndereco.close(true);
        }
    }

    public void pesquisar(String pesquisa) {
        ArrayList<String> listResultado = new ArrayList<>();

        for (int i = 0; i < listEnderecos.size(); i++) {
            if (listEnderecos.get(i).toLowerCase().contains(pesquisa.toLowerCase())) {
                Log.d("Endereco", listEnderecos.get(i));
                listResultado.add(listEnderecos.get(i));
            }
        }

        if (listResultado.size() > 0){

        }


        //listEnderecos.get(i).contains(pesquisa)
    }

    public void pesquisar3(String pesquisa) {
        Query query = collectionReference.orderBy("cep").startAt(pesquisa).endAt(pesquisa + "\uf8ff");
        FirestoreRecyclerOptions<Endereco> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Endereco>().setQuery(query, Endereco.class).build();
        enderecoAdapter = new EnderecoAdapter(firestoreRecyclerOptions);

        rcyEnderecos.setHasFixedSize(true);
        rcyEnderecos.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcyEnderecos.setAdapter(enderecoAdapter);

        enderecoAdapter.startListening();

        // getAdapterItemTouch();
        getRecyclerViewClickListener();
    }

    public void getListEnderecos() {
        final ArrayList<String> list = new ArrayList<>();
        CollectionReference collectionReference = firebaseFirestore.collection("Enderecos");

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Endereco endereco = documentSnapshot.toObject(Endereco.class);
                    list.add(endereco.getCEP() + " " + endereco.getRua() + " " + endereco.getCidade() + " " + endereco.getEstado());
                }
                listEnderecos = list;
            }
        });
    }

    //
    public void pesquisar2(String pesquisa) {
        if (listEnderecos.contains(pesquisa)) {
            Query query = collectionReference.whereEqualTo("cep", pesquisa);
            FirestoreRecyclerOptions<Endereco> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Endereco>().setQuery(query, Endereco.class).build();
            enderecoAdapter = new EnderecoAdapter(firestoreRecyclerOptions);

            rcyEnderecos.setHasFixedSize(true);
            rcyEnderecos.setLayoutManager(new LinearLayoutManager(getActivity()));
            rcyEnderecos.setAdapter(enderecoAdapter);

            enderecoAdapter.startListening();

            // getAdapterItemTouch();
            getRecyclerViewClickListener();
        } else {
            Toast.makeText(getActivity(), getString(R.string.endereco_nao_encontrado), Toast.LENGTH_SHORT).show();
        }
    }
    //

    public void getRecyclerViewClickListener() {
        enderecoAdapter.setRecyclerViewClickListener(new RecyclerViewClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int posicao) {
                //onDocument(documentSnapshot);
                Endereco endereco = documentSnapshot.toObject(Endereco.class);
                Intent intent = new Intent(getActivity(), EnderecoActivity.class);
                intent.putExtra("endereco", endereco);
                startActivity(intent);

                closeFloatingActionMenu();
            }

            @Override
            public boolean onItemLongClick(DocumentSnapshot documentSnapshot, int posicao) {
                Toast.makeText(getActivity(), "Longo" + documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                closeFloatingActionMenu();
                return true;
            }
        });
    }

    public void getListEnderecos2() {
        final ArrayList<String> list = new ArrayList<>();
        CollectionReference collectionReference = firebaseFirestore.collection("Enderecos");

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Endereco endereco = documentSnapshot.toObject(Endereco.class);
                    list.add(endereco.getCEP());
                }
                listEnderecos = list;
            }
        });
    }

    public void getFabXls() {
        fabXls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
                closeFloatingActionMenu();
            }
        });
    }

    public void getFabNovo() {
        fabNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFloatingActionMenu();
                Intent intent = new Intent(getActivity(), NovoEnderecoActivity.class);
                startActivity(intent);
            }
        });
    }

    public void getTextPesquisa(String texto) {
        if (texto != null) {
            pesquisar(texto);
        }
    }
}
