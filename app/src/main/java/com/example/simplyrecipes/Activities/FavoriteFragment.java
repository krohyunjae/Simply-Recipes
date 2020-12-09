package com.example.simplyrecipes.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplyrecipes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FavoriteFragment extends Fragment {

    FirebaseDatabase db;
    DatabaseReference reference;
    FirebaseAuth auth;
    List<Recipe> favoriteRecipes, filteredRecipes;
    RecyclerView favorite_recipe_recyclerview, filter_recyclerview;
    FavoriteAdapter adapter;
    FilterAdapter filterAdapter;
    ToggleButton mealTypeToggleBtn, cuisineToggleBtn, cookingTimeToggleBtn, ratingToggleBtn;
    TextView selectFilterTextView;
    ImageView exitFilterPopupImageView;
    PopupWindow popupWindow;
    Button applyFilterButton;
    LayoutInflater inflater;
    View popupView;
    Filter filters;
    HashMap<String, Set<String>> selectedFilters;
    Context context;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        favorite_recipe_recyclerview = view.findViewById(R.id.favorite_recipe_recyclerview);
        mealTypeToggleBtn = view.findViewById(R.id.meal_type_toggle_btn);
        cuisineToggleBtn = view.findViewById(R.id.cuisine_btn);
        cookingTimeToggleBtn = view.findViewById(R.id.cooking_time_btn);
        ratingToggleBtn = view.findViewById(R.id.rating_btn);
        favoriteRecipes = new ArrayList<>();
        filteredRecipes = new ArrayList<>();
        filters = new Filter();
        selectedFilters = new HashMap<>();
        context = getActivity().getApplicationContext();
        getFavoriteRecipes();
        addListenerOnToggleButtonClick();
    }


    private void applyFilter() {
        filteredRecipes.clear();
        int countedFilters = 0;
        int totalFilters = 0;

        for (Map.Entry<String, Set<String>> entry : selectedFilters.entrySet()) {
            totalFilters += entry.getValue().size();
        }

        if (selectedFilters.size() == 0) {
            filteredRecipes.addAll(favoriteRecipes);
            adapter.notifyDataSetChanged();
            return;
        }

        for (Recipe recipe: favoriteRecipes) {
            countedFilters = 0;
            if (selectedFilters.containsKey("Rating")) {
                double recipeRating = recipe.getRecipeRating() * 5 / 100;
                if (selectedFilters.get("Rating").contains("4.0 - 5.0") && recipeRating >= 4.0 && recipeRating <= 5.0) {
                    countedFilters += 1;
                } else if (selectedFilters.get("Rating").contains("3.0 - 4.0") && recipeRating >= 3.0 && recipeRating < 4.0) {
                    countedFilters += 1;
                } else if (selectedFilters.get("Rating").contains("2.0 - 3.0") && recipeRating >= 2.0 && recipeRating < 3.0) {
                    countedFilters += 1;
                } else if (selectedFilters.get("Rating").contains("1.0 - 2.0") && recipeRating >= 1.0 && recipeRating < 2.0) {
                    countedFilters += 1;
                }
            }

            if (selectedFilters.containsKey("Cooking Time")) {
                Set<String> ratingOptions = selectedFilters.get("Cooking Time");
                if (ratingOptions.contains("Less than 15 minutes".toLowerCase()) && recipe.getRecipeTime() < 15) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("15 - 30 minutes") && recipe.getRecipeTime() >= 15 && recipe.getRecipeTime() < 30) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("30 - 60 minutes") && recipe.getRecipeTime() >= 30 && recipe.getRecipeTime() < 60) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("60 - 120 minutes") && recipe.getRecipeTime() >= 60 && recipe.getRecipeTime() < 120) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("More than 120 minutes".toLowerCase()) && recipe.getRecipeTime() >= 120) {
                    countedFilters += 1;
                }
            }

            if (selectedFilters.containsKey("Meal Type")) {
                Set<String> ratingOptions = selectedFilters.get("Meal Type");
                if (ratingOptions.contains("breakfast") && recipe.getDishTypes().contains("breakfast")) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("brunch") && recipe.getDishTypes().contains("brunch")) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("main dish") && recipe.getDishTypes().contains("main dish")) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("lunch") && recipe.getDishTypes().contains("lunch")) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("dinner") && recipe.getDishTypes().contains("dinner")) {
                    countedFilters += 1;
                } else if (ratingOptions.contains("side dish") && recipe.getDishTypes().contains("side dish")) {
                    countedFilters += 1;
                }
            }

            if (selectedFilters.containsKey("Cuisine")) {
                Set<String> ratingOptions = selectedFilters.get("Cuisine");
                if (recipe.getCuisines() != null) {
                    if (ratingOptions.contains("chinese") && recipe.getCuisines().contains("Chinese")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("mexican") && recipe.getCuisines().contains("Mexican")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("italian") && recipe.getCuisines().contains("Italian")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("european") && recipe.getCuisines().contains("European")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("indian") && recipe.getCuisines().contains("Indian")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("caribbean") && recipe.getCuisines().contains("Caribbean")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("mediterranean") && recipe.getCuisines().contains("Mediterranean")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("asian") && recipe.getCuisines().contains("Asian")) {
                        countedFilters += 1;
                    } else if (ratingOptions.contains("japanese") && recipe.getCuisines().contains("Japanese")) {
                        countedFilters += 1;
                    }
                }
            }

            if (countedFilters == totalFilters) {
                filteredRecipes.add(recipe);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setDishTypes(final DataSnapshot ds, final int index, DatabaseReference reference) {
        final List<String> dishTypes = new ArrayList<>();
        reference.child("Dish Types").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot dishType : ds.getChildren()) {
                    dishTypes.add(dishType.getValue().toString());
                }
                favoriteRecipes.get(index).setDishTypes(dishTypes);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setCuisines(final DataSnapshot ds, final int index, DatabaseReference reference) {
        final List<String> cuisines = new ArrayList<>();
        reference.child("Cuisines").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot dishType : ds.getChildren()) {
                    cuisines.add(dishType.getValue().toString());
                }
                favoriteRecipes.get(index).setCuisines(cuisines);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFavoriteRecipes() {
        reference = FirebaseDatabase.getInstance().getReference("users/" + auth.getCurrentUser().getUid() + "/Favorite");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                favoriteRecipes.clear();
                filteredRecipes.clear();
                for (final DataSnapshot snap : snapshot.getChildren()) {

                    if (!snap.getKey().toString().equals("none")) {
                        int recipeID = Integer.parseInt(snap.getKey().toString());
                        String recipeName = null;
                        String recipeURL = null;
                        String recipeTime = null;
                        String recipeRating = null;

                        for (final DataSnapshot ds : snap.getChildren()) {
                            if (ds.getKey().toString().equals("Dish Types")) {
                                setDishTypes(ds, index, reference);
                            } else if (ds.getKey().toString().equals("Cuisines")) {
                                setCuisines(ds, index, reference);
                            } else if (ds.getKey().toString().equals("Recipe Name")) {
                                recipeName = ds.getValue().toString();
                            } else if (ds.getKey().toString().equals("Recipe Time")) {
                                recipeTime = ds.getValue().toString();
                            } else if (ds.getKey().toString().equals("Recipe URL")) {
                                recipeURL = ds.getValue().toString();
                            } else if (ds.getKey().toString().equals("Recipe Rating")) {
                                recipeRating = ds.getValue().toString();
                            }
                        }
                        if (recipeRating == null || recipeTime == null) {
                            recipeTime = "-1";
                            recipeRating = "-1";
                        }
                        Recipe currRecipe = new Recipe(recipeID, recipeName, recipeURL, Integer.parseInt(recipeTime), Double.parseDouble(recipeRating));
                        favoriteRecipes.add(currRecipe);

                        // on the off chance that spoonacular has some missing arguments
                        if (recipeName == null) {
                            recipeName = "";
                        }
                        if (recipeTime == null) {
                            recipeTime = "-1";
                        }
                        if (recipeURL == null) {
                            recipeURL = "";
                        }
                        if (recipeRating == null) {
                            recipeRating = "-1";
                        }
                    }
                    index += 1;
                }

                filteredRecipes.addAll(favoriteRecipes);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new FavoriteAdapter(getActivity().getApplicationContext(), filteredRecipes);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

                        favorite_recipe_recyclerview.setLayoutManager(layoutManager);
                        favorite_recipe_recyclerview.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error" + error.getMessage());
            }
        });
    }

    private void addListenerOnToggleButtonClick() {
        mealTypeToggleBtn.setOnCheckedChangeListener(handleOnClick(mealTypeToggleBtn));
        cuisineToggleBtn.setOnCheckedChangeListener(handleOnClick(cuisineToggleBtn));
        ratingToggleBtn.setOnCheckedChangeListener(handleOnClick(ratingToggleBtn));
        cookingTimeToggleBtn.setOnCheckedChangeListener(handleOnClick(cookingTimeToggleBtn));
    }

    CompoundButton.OnCheckedChangeListener handleOnClick(final ToggleButton button) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showPopupFilter(buttonView);
                } else {
                    if (buttonView.equals(mealTypeToggleBtn) && selectedFilters.containsKey("Meal Type")) {
                        if (selectedFilters.get("Meal Type").size() > 0) {
                            mealTypeToggleBtn.setChecked(true);
                            showPopupFilter(buttonView);
                        }
                    } else if (buttonView.equals(cookingTimeToggleBtn) && selectedFilters.containsKey("Cooking Time")) {
                        if (selectedFilters.get("Cooking Time").size() > 0) {
                            cookingTimeToggleBtn.setChecked(true);
                            showPopupFilter(buttonView);
                        }
                    } else if (buttonView.equals(cuisineToggleBtn) && selectedFilters.containsKey("Cuisine")) {
                        if (selectedFilters.get("Cuisine").size() > 0) {
                            cuisineToggleBtn.setChecked(true);
                            showPopupFilter(buttonView);
                        }
                    } if (buttonView.equals(ratingToggleBtn) && selectedFilters.containsKey("Rating")) {
                        if (selectedFilters.get("Rating").size() > 0) {
                            ratingToggleBtn.setChecked(true);
                            showPopupFilter(buttonView);
                        }
                    }
                }
            }
        };
    }

    private void showPopupFilter(final CompoundButton view) {
        // inflate the layout of the popup window
        inflater = getActivity().getLayoutInflater();
        popupView = inflater.inflate(R.layout.filter_popup_layout, null);
        popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!selectedFilters.containsKey("Meal Type")) {
                    mealTypeToggleBtn.setChecked(false);
                }
                if (!selectedFilters.containsKey("Cooking Time")) {
                    cookingTimeToggleBtn.setChecked(false);
                }
                if (!selectedFilters.containsKey("Cuisine")) {
                    cuisineToggleBtn.setChecked(false);
                }
                if (!selectedFilters.containsKey("Rating")) {
                    ratingToggleBtn.setChecked(false);
                }
            }
        });

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        filter_recyclerview = popupView.findViewById(R.id.filter_recyclerview);
        selectFilterTextView = popupView.findViewById(R.id.select_filter_tv);
        exitFilterPopupImageView = popupView.findViewById(R.id.exit_button);
        applyFilterButton = popupView.findViewById(R.id.apply_filter_button);
        addListenerForPopUpView(view);
        setFilterAdapter(view);
    }

    private void addListenerForPopUpView(final CompoundButton view) {
        exitFilterPopupImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        applyFilterButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (view.equals(mealTypeToggleBtn)) {
                    if (filterAdapter.getSelectedFilters().size() == 0) {
                        selectedFilters.remove("Meal Type");
                        view.setChecked(false);
                    } else {
                        selectedFilters.put("Meal Type", filterAdapter.getSelectedFilters());
                        view.setChecked(true);
                    }
                    selectedFilters.put("Meal Type", filterAdapter.getSelectedFilters());
                } else if (view.equals(cookingTimeToggleBtn)) {
                    if (filterAdapter.getSelectedFilters().size() == 0) {
                        selectedFilters.remove("Cooking Time");
                        view.setChecked(false);
                    } else {
                        selectedFilters.put("Cooking Time", filterAdapter.getSelectedFilters());
                        view.setChecked(true);
                    }
                } else if (view.equals(cuisineToggleBtn)) {
                    if (filterAdapter.getSelectedFilters().size() == 0) {
                        selectedFilters.remove("Cuisine");
                        view.setChecked(false);
                    } else {
                        selectedFilters.put("Cuisine", filterAdapter.getSelectedFilters());
                        view.setChecked(true);
                    }
                } else if (view.equals(ratingToggleBtn)) {
                    if (filterAdapter.getSelectedFilters().size() == 0) {
                        selectedFilters.remove("Rating");
                        view.setChecked(false);
                    } else {
                        selectedFilters.put("Rating", filterAdapter.getSelectedFilters());
                        view.setChecked(true);
                    }
                }
                applyFilter();
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void setFilterAdapter(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        filter_recyclerview.setLayoutManager(layoutManager);

        if (view.equals(mealTypeToggleBtn)) {
            selectFilterTextView.setText("Select Meal Type");
            filterAdapter = new FilterAdapter(context, filters.getMealTypeOptions(), selectedFilters.get("Meal Type"));
        } else if (view.equals(cookingTimeToggleBtn)) {
            selectFilterTextView.setText("Select Cooking Time");
            filterAdapter = new FilterAdapter(context, filters.getCookingTimeOptions(), selectedFilters.get("Cooking Time"));
        } else if (view.equals(cuisineToggleBtn)) {
            selectFilterTextView.setText("Select Cuisine");
            filterAdapter = new FilterAdapter(context, filters.getCuisineOptions(), selectedFilters.get("Cuisine"));
        } else if (view.equals(ratingToggleBtn)) {
            selectFilterTextView.setText("Select Rating");
            filterAdapter = new FilterAdapter(context, filters.getRatingOptions(), selectedFilters.get("Rating"));
        }
        filter_recyclerview.setAdapter(filterAdapter);
    }
}
