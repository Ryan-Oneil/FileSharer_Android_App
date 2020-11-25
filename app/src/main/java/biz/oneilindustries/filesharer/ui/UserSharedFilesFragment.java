package biz.oneilindustries.filesharer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Comparator;

import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.R;
import biz.oneilindustries.filesharer.listadapters.SharedLinkAdapter;
import biz.oneilindustries.filesharer.service.FileShareService;

public class UserSharedFilesFragment extends Fragment {

    private SharedLinkAdapter sharedLinkAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_files, container, false);
        FileShareService fileShareService = new FileShareService(root.getContext());
        ArrayList<Link> links = fileShareService.getUserLinks();

        if (links.isEmpty()) {
            links = (ArrayList<Link>) fileShareService.fetchUserLinks();
        }
        links.sort(Comparator.comparing(Link::getCreationDate).reversed());

        sharedLinkAdapter = new SharedLinkAdapter(root.getContext(), R.layout.my_files_list_item, R.id.my_shared_files_list, links, getFragmentManager());
        ListView listView = root.findViewById(R.id.my_shared_files_list);

        Button refreshLinks = root.findViewById(R.id.fetchLinksButton);
        refreshLinks.setOnClickListener(v -> {
            fileShareService.fetchUserLinks();

            ArrayList<Link> newLinks = fileShareService.getUserLinks();
            newLinks.sort(Comparator.comparing(Link::getCreationDate).reversed());

            sharedLinkAdapter.clear();
            newLinks.forEach(link -> sharedLinkAdapter.add(link));
        });
        listView.setAdapter(sharedLinkAdapter);

        return root;
    }
}