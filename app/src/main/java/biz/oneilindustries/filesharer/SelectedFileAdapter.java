package biz.oneilindustries.filesharer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class SelectedFileAdapter extends ArrayAdapter<File> {

    private ArrayList<File> data;

    public SelectedFileAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull ArrayList<File> data) {
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
        textNumber.setText(String.format("Size : %s", humanReadableByteCountSI(this.data.get(position).length())));

        Button manageButton = row.findViewById(R.id.delete_file_button);
        manageButton.setOnClickListener(v -> this.remove(data.get(position)));

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
