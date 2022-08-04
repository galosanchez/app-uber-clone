package com.galosanchez.appuberclone.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.galosanchez.appuberclone.R;


public class ProgressDialogFragment extends DialogFragment {

    private TextView textViewMesagge;
    private String message;
    private FragmentManager fragmentManager;
    private String tag;

    public ProgressDialogFragment(FragmentManager fragmentManager, String message, String tag) {
        this.message = message;
        this.fragmentManager = fragmentManager;
        this.tag = tag;
    }

    public ProgressDialogFragment(FragmentManager fragmentManager, String tag) {
        this.message = "Cargando...";
        this.fragmentManager = fragmentManager;
        this.tag = tag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        this.setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_progressdialog, null);
        textViewMesagge = view.findViewById(R.id.textViewMesagge);
        textViewMesagge.setText(message);
        builder.setView(view);


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public void closeDialog(){
        dismiss();
    }

    public void showDialog(){
        this.show(this.fragmentManager,this.tag);
    }

    public void setMessage(String message){
        textViewMesagge.setText(message);
    }

}