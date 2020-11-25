package biz.oneilindustries.filesharer.listadapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.ui.ManageLinkFragment;

public class SharedLinkAdapter extends ArrayAdapter<Link> {

    private ArrayList<Link> data;
    private FragmentManager fragmentManager;

    public SharedLinkAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull ArrayList<Link> data, FragmentManager fragmentManager) {
        super(context, resource, textViewResourceId, data);
        this.data = data;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View row = inflater.inflate(R.layout.my_files_list_item, parent, false);

        TextView textView = row.findViewById(R.id.link_title);
        textView.setText(this.data.get(position).getTitle().isEmpty() ? "No Title" : this.data.get(position).getTitle());

        TextView textNumber = row.findViewById(R.id.link_details);
        textNumber.setText(String.format("Created : %s", this.data.get(position).getCreationDate().toString()));

        Button manageButton = row.findViewById(R.id.manage_link_button);

        manageButton.setOnClickListener(v -> {
            Fragment detailFragment = new ManageLinkFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", this.data.get(position).getId());
            detailFragment.setArguments(bundle);


            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.nav_host_fragment, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        return row;
    }
}
