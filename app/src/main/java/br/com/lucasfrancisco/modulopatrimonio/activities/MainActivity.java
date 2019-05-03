package br.com.lucasfrancisco.modulopatrimonio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.lucasfrancisco.modulopatrimonio.R;
import br.com.lucasfrancisco.modulopatrimonio.dao.preferences.SharedPreferencesEmpresa;
import br.com.lucasfrancisco.modulopatrimonio.fragments.EmpresaFragment;
import br.com.lucasfrancisco.modulopatrimonio.fragments.EnderecoFragment;
import br.com.lucasfrancisco.modulopatrimonio.fragments.ObjetoFragment;
import br.com.lucasfrancisco.modulopatrimonio.fragments.PatrimonioFragment;
import br.com.lucasfrancisco.modulopatrimonio.fragments.PesquisaFragment;
import br.com.lucasfrancisco.modulopatrimonio.interfaces.CommunicatePesquisaFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, CommunicatePesquisaFragment {
    private long backPressedTime;
    private Toast backToast;

    private DrawerLayout dwlMain;
    private Toolbar tbrTopMain;
    private BottomNavigationView bnvBottom;
    private NavigationView ngvMain;

    // header_usuario
    private ImageView imvPerfil;
    private TextView tvNome, tvEmail;

    // Fragment
    private Fragment fragment = null;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Buscar as empresas no Firestore e salva com SharedPreferencesEmpresa
        SharedPreferencesEmpresa sharedPreferencesEmpresa = new SharedPreferencesEmpresa();
        sharedPreferencesEmpresa.inserir(getApplicationContext());

        // Tool bar superior
        tbrTopMain = (Toolbar) findViewById(R.id.tbrTopMain);
        setSupportActionBar(tbrTopMain);

        // Tool bar inferior
        bnvBottom = (BottomNavigationView) findViewById(R.id.bnvBottom);
        getBnvBottom();

        dwlMain = (DrawerLayout) findViewById(R.id.dwlMain);

        // NavigationView
        ngvMain = (NavigationView) findViewById(R.id.ngvMain);
        ngvMain.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, dwlMain, tbrTopMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        dwlMain.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Fragment inicial
        getSupportFragmentManager().beginTransaction().replace(R.id.fmlPesquisa, new PesquisaFragment()).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.fmlConteudo, new PatrimonioFragment()).commit();
        setFragment(new PatrimonioFragment());

        // header_usuario
        View headerUsuario = ngvMain.getHeaderView(0);
        imvPerfil = (ImageView) headerUsuario.findViewById(R.id.imvPerfil);
        tvNome = (TextView) headerUsuario.findViewById(R.id.tvNome);
        tvEmail = (TextView) headerUsuario.findViewById(R.id.tvEmail);
        setHeaderUsuario();
    }

    @Override
    public void onBackPressed() {
        if (dwlMain.isDrawerOpen(GravityCompat.START)) {
            dwlMain.closeDrawer(GravityCompat.START);
        }

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getApplicationContext(), getString(R.string.pressione_novamente_para_sair), Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.itConfiguracoes:
                break;
            case R.id.itPerfil:
                break;
            case R.id.itSair:
                signOutGoogle();
                break;
            case R.id.itCompartilhar:
                break;
        }
        dwlMain.closeDrawer(GravityCompat.START);

        return true;
    }

    // Dados entre Fragments
    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onSetText(String texto, String filtro, long limite) {
        Fragment fragment = getFragment();

        if (fragment instanceof PatrimonioFragment) {
            PatrimonioFragment patrimonioFragment = (PatrimonioFragment) getSupportFragmentManager().findFragmentById(R.id.fmlConteudo);
            patrimonioFragment.pesquisar(texto, filtro, limite);
        } else if (fragment instanceof EmpresaFragment) {
            EmpresaFragment empresaFragment = (EmpresaFragment) getSupportFragmentManager().findFragmentById(R.id.fmlConteudo);
            empresaFragment.pesquisar(texto, filtro, limite);
        } else if (fragment instanceof EnderecoFragment) {
            EnderecoFragment enderecoFragment = (EnderecoFragment) getSupportFragmentManager().findFragmentById(R.id.fmlConteudo);
            enderecoFragment.pesquisar(texto, filtro, limite);
        } else if (fragment instanceof ObjetoFragment) {
            ObjetoFragment objetoFragment = (ObjetoFragment) getSupportFragmentManager().findFragmentById(R.id.fmlConteudo);
            objetoFragment.pesquisar(texto, filtro, limite);
        } else {
            Log.d("FRAGMENT", "Sem opção");
        }
    }

    @Override
    public void onSetFilter(ArrayList arrayList) { // Passa o ArrayList que veio dos fragments para o PesquisaFragment
        PesquisaFragment pesquisaFragment = (PesquisaFragment) getSupportFragmentManager().findFragmentById(R.id.fmlPesquisa);
        pesquisaFragment.setFilter(arrayList);
    }

    // Toolbar inferior
    public void getBnvBottom() {
        bnvBottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.itPatrimonio:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlPesquisa, new PesquisaFragment()).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlConteudo, new PatrimonioFragment()).commit();
                        setFragment(new PatrimonioFragment());
                        break;
                    case R.id.itEmpresa:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlPesquisa, new PesquisaFragment()).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlConteudo, new EmpresaFragment()).commit();
                        setFragment(new EmpresaFragment());
                        break;
                    case R.id.itEndereco:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlPesquisa, new PesquisaFragment()).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlConteudo, new EnderecoFragment()).commit();
                        setFragment(new EnderecoFragment());
                        break;
                    case R.id.itObjeto:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlPesquisa, new PesquisaFragment()).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fmlConteudo, new ObjetoFragment()).commit();
                        setFragment(new ObjetoFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void setHeaderUsuario() {
        tvNome.setText(firebaseUser.getDisplayName());
        tvEmail.setText(firebaseUser.getEmail());
        Picasso.with(getApplicationContext()).load(firebaseUser.getPhotoUrl()).placeholder(R.drawable.ic_image).into(imvPerfil);
    }

    private void signOutGoogle() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
