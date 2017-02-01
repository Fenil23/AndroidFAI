package com.example.mind.androidfai;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;

import java.util.ArrayList;
import java.util.List;


public class AppIndexingService extends IntentService {

    public AppIndexingService() {
        super("AppIndexingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<Indexable> indexableNotes = new ArrayList<>();

        for (Restaurant restaurant : getAllRecipes()) {
            if (restaurant != null) {
                Indexable noteToIndex = Indexables.noteDigitalDocumentBuilder()
                        .setName(restaurant.getName() + "Restaurant")
                        .setUrl(restaurant.getCuisineUrl())
                        .build();

                indexableNotes.add(noteToIndex);
            }
        }

        if (indexableNotes.size() > 0) {
            Indexable[] notesArr = new Indexable[indexableNotes.size()];
            notesArr = indexableNotes.toArray(notesArr);

            // batch insert indexable notes into index
            FirebaseAppIndex.getInstance().update(notesArr);
        }
    }

    private List<Restaurant> getAllRecipes() {
        ArrayList recipesList = new ArrayList();
        // TODO: Exercise - access all recipes with their notes from the database here.
        return recipesList;
    }
}
