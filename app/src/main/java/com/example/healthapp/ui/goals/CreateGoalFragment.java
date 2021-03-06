package com.example.healthapp.ui.goals;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.healthapp.R;
import com.example.healthapp.backend.goals.RESTTaskSubmitGoal;
import com.example.healthapp.ui.MainActivity;

import java.util.Collections;
import java.util.HashMap;

public class CreateGoalFragment extends Fragment {

    private static HashMap<String, Class<? extends GoalTypeFragment>> goalTypesRegistry = new HashMap<>();

    static {
        goalTypesRegistry.put("Calories Consumed",     CaloriesConsumedGoalFragment.class);
        goalTypesRegistry.put("Calories Burned",       CaloriesBurnedGoalFragment  .class);
        goalTypesRegistry.put("Exercise Duration",     ExerciseDurationGoalFragment.class);
        goalTypesRegistry.put("Exercise Quantity",     ExerciseQuantityGoalFragment.class);
        goalTypesRegistry.put("Amount of Sleep",       SleepGoalFragment           .class);
        goalTypesRegistry.put("Steps/Distance Walked", StepGoalFragment            .class);
        goalTypesRegistry.put("Water Intake",          WaterGoalFragment           .class);
    }

    public CreateGoalFragment() { super(R.layout.fragment_create_goal); }

    private Spinner goalTypeSpinner;
    private GoalTypeFragment currFrag;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().findViewById(R.id.buttonSubmitGoal).setOnClickListener(v -> onSubmitClicked());
        getActivity().findViewById(R.id.buttonSubmitGoalCancel).setOnClickListener(v -> onCancelClicked());

        ArrayAdapter<String> adap = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.list(Collections.enumeration(goalTypesRegistry.keySet())));
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalTypeSpinner = (Spinner)getActivity().findViewById(R.id.spinnerGoalType);
        goalTypeSpinner.setAdapter(adap);

        goalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { updateGoalTypeShown(); }
            @Override public void onNothingSelected(AdapterView<?> adapterView) { updateGoalTypeShown(); }
        });
    }

    private void updateGoalTypeShown() {
        Class<? extends GoalTypeFragment> goalType = goalTypesRegistry.get((String)goalTypeSpinner.getSelectedItem());
        if(goalType == null) return;

        try {
            currFrag = goalType.newInstance();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        getActivity().getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.goalFragHolder, currFrag, null)
            .addToBackStack(null)
            .commit();
    }

    private void onCancelClicked() {
        ((MainActivity)getActivity()).showFrag(GoalsFragment.class);
    }

    private void error(String msg) {
        new AlertDialog.Builder(getActivity()).setMessage(msg).setNeutralButton("OK", null).show();
    }

    private void onSubmitClicked() {
        String name = ((EditText)getActivity().findViewById(R.id.editTextGoalName)).getText().toString();
        boolean active = ((CheckBox)getActivity().findViewById(R.id.checkBoxGoalActive)).isChecked();

        if(name.length() == 0) {
            error("Please input a name for the goal.");
        } else if(currFrag == null) {
            error("Please select a goal type");
        } else {
            currFrag.onSubmitClicked(goal -> {
                goal.setName(name);
                goal.setActive(active);

                RESTTaskSubmitGoal.enqueue(goal, () -> {
                    error("Goal submitted!");
                    ((MainActivity)getActivity()).showFrag(GoalsFragment.class);
                }, err -> error("That goal name is already in use, please choose another."));
            }, this::error);
        }
    }
}