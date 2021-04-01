package br.ucs.android.jogo_velha_selfies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import br.ucs.android.jogo_velha_selfies.classes.ResizableButton;
import br.ucs.android.jogo_velha_selfies.classes.ResizableImageButton;

public class MainActivity extends AppCompatActivity {

    Toast currentToast;
    Bitmap player1Image;
    Bitmap player2Image;
    TextView tipTextView;
    boolean isPlaying = false;
    int currentPlayerTurn = -1;

    static final int REQUEST_IMAGE_CAPTURE_PLAYER1 = 1;
    static final int REQUEST_IMAGE_CAPTURE_PLAYER2 = 2;

    ColorStateList stateListSelected;
    ColorStateList stateListDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Jogo da Velha de Selfies");

        tipTextView = (TextView) findViewById(R.id.tip_text);
        tipTextView.setText("Tire as selfies dos dois jogadores para iniciar o jogo!");

        stateListSelected = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.selectedPlayerColor));
        stateListDefault = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.defaultBackgroundColor));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE_PLAYER1 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            ResizableImageButton btn = (ResizableImageButton) findViewById(R.id.btn_player_1);
            //btn.setBackground(new BitmapDrawable(getResources(), photo));
            //btn.setText("");
            btn.setImageBitmap(photo);
            player1Image = photo;
            verifyIfGameCanStart();
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE_PLAYER2 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            ResizableImageButton btn = (ResizableImageButton) findViewById(R.id.btn_player_2);
            //btn.setBackground(new BitmapDrawable(getResources(), photo));
            //btn.setText("");
            btn.setImageBitmap(photo);
            player2Image = photo;
            verifyIfGameCanStart();
        }

    }

    public void onGridButtonClicked(View view) {

        String tag = (String) view.getTag();

        int rowNumber = Integer.parseInt(tag.substring(0, 1));
        int colNumber = Integer.parseInt(tag.substring(1, 2));

        if (player1Image == null) {
            showToast("Selfie do jogador 1 é necessária para jogar");
            return;
        }

        if (player2Image == null) {
            showToast("Selfie do jogador 1 é necessária para jogar");
            return;
        }

        if (currentPlayerTurn == -1) {
            return;
        }

        ((ResizableImageButton) view).setImageBitmap(currentPlayerTurn == 1 ? player1Image : player2Image);
        changeTurn(-1);

    }

    public void onPlayer1ImageClicked(View view) {

        if (player1Image == null) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE_PLAYER1);
        }

        if (isPlaying && currentPlayerTurn == -1) {
            changeTurn(1);
        }

    }

    public void onPlayer2ImageClicked(View view) {

        if (player2Image == null) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE_PLAYER2);
        }

        if (isPlaying && currentPlayerTurn == -1) {
            changeTurn(2);
        }

    }

    private void changeTurn(int turn) {

        if (turn > 0) {
            currentPlayerTurn = turn;
        } else {
            currentPlayerTurn = currentPlayerTurn == 1 ? 2 : 1;
        }

        ResizableImageButton btnPlayer1 = findViewById(R.id.btn_player_1);
        ResizableImageButton btnPlayer2 = findViewById(R.id.btn_player_2);

        if (currentPlayerTurn == 1) {
            btnPlayer1.setBackgroundTintList(stateListSelected);
            btnPlayer2.setBackgroundTintList(stateListDefault);
        } else {
            btnPlayer1.setBackgroundTintList(stateListDefault);
            btnPlayer2.setBackgroundTintList(stateListSelected);
        }

        tipTextView.setText("Vez do jogador " + currentPlayerTurn + ". Selecione uma posição!");

    }

    private void showToast(String msg) {

        if (currentToast != null) {
            currentToast.cancel();
        }

        this.currentToast = Toast.makeText(this.getBaseContext(), msg, Toast.LENGTH_SHORT);
        this.currentToast.show();
    }

    private void verifyIfGameCanStart() {

        if (player1Image == null || player2Image == null) {
            return;
        }

        isPlaying = true;
        tipTextView.setText("Selecione quem jogará primeiro!");

    }

}