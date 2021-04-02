package br.ucs.android.jogo_velha_selfies;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import br.ucs.android.jogo_velha_selfies.classes.ResizableButton;
import br.ucs.android.jogo_velha_selfies.classes.ResizableImageButton;

public class MainActivity extends AppCompatActivity {

    Toast currentToast;
    Bitmap player1Image;
    Bitmap player2Image;
    TextView tipTextView;
    boolean isPlaying = false;
    boolean hasWinner = false;
    int currentPlayerTurn = -1;
    int winnerPlayer = -1;

    int [][] position = new int[3][3];

    static final int REQUEST_IMAGE_CAPTURE_PLAYER1 = 1;
    static final int REQUEST_IMAGE_CAPTURE_PLAYER2 = 2;

    ColorStateList stateListSelected;
    ColorStateList stateListDefault;
    ColorStateList stateListWinner;

    ResizableImageButton btnPlayer1;
    ResizableImageButton btnPlayer2;

    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.game_title);

        tipTextView = (TextView) findViewById(R.id.tip_text);

        stateListSelected = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.selectedPlayerColor));
        stateListDefault = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.defaultBackgroundColor));
        stateListWinner = ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.winnerBackgroundColor));

        btnPlayer1 = findViewById(R.id.btn_player_1);
        btnPlayer2 = findViewById(R.id.btn_player_2);

        tipTextView.setText(R.string.take_selfies);
        clearPositions();

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
            showToast("Selecione qual jogar deve começar!");
            return;
        }

        if (hasWinner) {
            return;
        }

        if (this.position[rowNumber][colNumber] != -1) {
            showToast("Esta posição já está preenchida!");
            return;
        }

        this.position[rowNumber][colNumber] = currentPlayerTurn;
        ((ResizableImageButton) view).setImageBitmap(currentPlayerTurn == 1 ? player1Image : player2Image);

        if (gameHasWinner()) {
            return;
        }

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

        clearPositions();
        isPlaying = false;
        currentPlayerTurn = -1;
        hasWinner = false;

        btnPlayer2.setBackgroundTintList(stateListDefault);
        btnPlayer1.setBackgroundTintList(stateListDefault);

        if (view == null) {
            isPlaying = true;
            tipTextView.setText(R.string.select_first_player);
            return;
        }

        player1Image = null;
        player2Image = null;

        btnPlayer1.setImageResource(R.drawable.player_1_image);
        btnPlayer2.setImageResource(R.drawable.player_2_image);

    }

    private void clearPositions() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.position[i][j] = -1;
                resetGridButtonByTag(i + "" + j);
            }
        }
    }

    private boolean gameHasWinner() {

        boolean hasWinner = false;

        // full row
        for (int i = 0; i < 3; i++) {

            if (this.position[i][0] == this.position[i][1] && this.position[i][1] == this.position[i][2]) {

                if (this.position[i][0] == -1) continue;

                getViewByTag(i + "0").setBackgroundTintList(stateListWinner);
                getViewByTag(i + "1").setBackgroundTintList(stateListWinner);
                getViewByTag(i + "2").setBackgroundTintList(stateListWinner);
                hasWinner = true;
                winnerPlayer = this.position[i][0];
                break;
            }

        }

        // full column
        if (!hasWinner) {
            for (int i = 0; i < 3; i++) {

                if (this.position[0][i] == this.position[1][i] && this.position[1][i] == this.position[2][i]) {

                    if (this.position[0][i] == -1) continue;

                    getViewByTag("0" + i).setBackgroundTintList(stateListWinner);
                    getViewByTag("1" + i).setBackgroundTintList(stateListWinner);
                    getViewByTag("2" + i).setBackgroundTintList(stateListWinner);
                    hasWinner = true;
                    winnerPlayer = this.position[0][i];
                    break;
                }

            }
        }

        // diagonal
        if (!hasWinner) {

            if (this.position[0][0] == this.position[1][1] && this.position[1][1] == this.position[2][2]) {

                if (this.position[0][0] != -1) {
                    getViewByTag("00").setBackgroundTintList(stateListWinner);
                    getViewByTag("11").setBackgroundTintList(stateListWinner);
                    getViewByTag("22").setBackgroundTintList(stateListWinner);
                    hasWinner = true;
                    winnerPlayer = this.position[0][0];
                }

            }
        }

        if (!hasWinner) {
            if (this.position[0][2] == this.position[1][1] && this.position[1][1] == this.position[2][0]) {

                if (this.position[0][2] != -1) {
                    getViewByTag("02").setBackgroundTintList(stateListWinner);
                    getViewByTag("11").setBackgroundTintList(stateListWinner);
                    getViewByTag("20").setBackgroundTintList(stateListWinner);
                    hasWinner = true;
                    winnerPlayer = this.position[0][2];
                }

            }
        }

        if (hasWinner) {
            this.hasWinner = true;
            setTimeout(() -> {

                this.runOnUiThread(() -> {

                    LayoutInflater li = getLayoutInflater();
                    View view = li.inflate(R.layout.winner_alert, null);

                    ImageView img = (ImageView) view.findViewById(R.id.imageView);
                    img.setImageBitmap(winnerPlayer == 1 ? player1Image : player2Image);

                    Button dismissBtn = (Button) view.findViewById(R.id.btn_dismiss);
                    dismissBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)
                        {
                            alert.cancel();
                            restartGame(null);
                        }
                    });

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Vencedor");
                    builder.setView(view);
                    alert = builder.create();
                    alert.show();

                });

            }, 3000);
            return true;
        }

        boolean hasBlankSpaces = false;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                int val = this.position[i][j];

                if (val == -1) {
                    hasBlankSpaces = true;
                    break;
                }

            }
            if (hasBlankSpaces) {
                break;
            }
        }

        if (!hasBlankSpaces) {
            System.out.println("deu VELHA!");
        }

        return false;
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

    private void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

}