package biz.oneilindustries.filesharer.listadapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.DTO.SharedFile;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.service.FileShareService;
import biz.oneilindustries.filesharer.ui.ManageLinkFragment;

public class SharedFileAdapter extends ArrayAdapter<SharedFile> {

    private ArrayList<SharedFile> data;

    public SharedFileAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull ArrayList<SharedFile> data) {
        super(context, resource, textViewResourceId, data);
        this.data = data;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View row = inflater.inflate(R.layout.uploaded_files_list_item, parent, false);

        TextView textView = row.findViewById(R.id.file_name);
        textView.setText(this.data.get(position).getName());

        TextView textNumber = row.findViewById(R.id.file_size);
        textNumber.setText(String.format("Size : %s", humanReadableByteCountSI(this.data.get(position).getSize())));

        Button manageButton = row.findViewById(R.id.delete_file_button);

        manageButton.setOnClickListener(v -> {
            FileShareService fileShareService = new FileShareService(this.getContext());

            String response = fileShareService.deleteFile(data.get(position));
            this.remove(data.get(position));

            Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
        });
        return row;
    }

    //https://stackoverflow.com/questions/3758606/how-can-i-convert-byte-size-into-a-human-readable-format-in-java
    public String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
