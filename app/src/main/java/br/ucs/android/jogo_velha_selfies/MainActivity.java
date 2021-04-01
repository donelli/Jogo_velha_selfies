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
import android.widget.GridLayout;
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

    ResizableImageButton btnPlayer1;
    ResizableImageButton btnPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.game_title);

        tipTextView = (TextView) findViewById(R.id.tip_text);
        tipTextView.setText(R.string.take_selfies);

        stateListSelected = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.selectedPlayerColor));
        stateListDefault = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.defaultBackgroundColor));

        btnPlayer1 = findViewById(R.id.btn_player_1);
        btnPlayer2 = findViewById(R.id.btn_player_2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE_PLAYER1 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            btnPlayer1.setImageBitmap(photo);
            player1Image = photo;
            verifyIfGameCanStart();
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE_PLAYER2 && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            btnPlayer2.setImageBitmap(photo);
            player2Image = photo;
            verifyIfGameCanStart();
        }

    }

    public void onGridButtonClicked(View view) {

        String tag = (String) view.getTag();

        int rowNumber = Integer.parseInt(tag.substring(0, 1));
        int colNumber = Integer.parseInt(tag.substring(1, 2));

        if (player1Image == null) {
            showToast(getString(R.string.first_player_photo_required));
            return;
        }

        if (player2Image == null) {
            showToast(getString(R.string.second_player_photo_required));
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

    public void restartGame(View view) {

        player1Image = null;
        player2Image = null;
        isPlaying = false;
        currentPlayerTurn = -1;

        tipTextView.setText(R.string.take_selfies);

        btnPlayer1.setBackgroundTintList(stateListDefault);
        btnPlayer1.setImageResource(R.drawable.player_1_image);
        btnPlayer2.setBackgroundTintList(stateListDefault);
        btnPlayer2.setImageResource(R.drawable.player_2_image);

        resetGridButtonByTag("00");
        resetGridButtonByTag("01");
        resetGridButtonByTag("01");
        resetGridButtonByTag("10");
        resetGridButtonByTag("11");
        resetGridButtonByTag("11");
        resetGridButtonByTag("20");
        resetGridButtonByTag("21");
        resetGridButtonByTag("21");

    }

    private void resetGridButtonByTag(String tag) {
        ResizableImageButton btn = (ResizableImageButton) getViewByTag(tag);
        btn.setBackgroundTintList(stateListDefault);
        btn.setImageBitmap(null);
    }

    private View getViewByTag(String tag) {
        GridLayout gl = (GridLayout)findViewById(R.id.grid_layout);
        return (View) gl.findViewWithTag(tag);
    }

    private void changeTurn(int turn) {

        if (turn > 0) {
            currentPlayerTurn = turn;
        } else {
            currentPlayerTurn = currentPlayerTurn == 1 ? 2 : 1;
        }

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
        tipTextView.setText(R.string.select_first_player);

    }

}