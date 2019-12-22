package com.trufla.androidtruforms.truviews;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.trufla.androidtruforms.R;
import com.trufla.androidtruforms.interfaces.FormContract;
import com.trufla.androidtruforms.interfaces.TruConsumer;
import com.trufla.androidtruforms.models.DataInstance;
import com.trufla.androidtruforms.models.EnumInstance;

import java.util.ArrayList;

public class TruEnumDataView extends TruEnumView {
    private int selectedPosition = -1;
    private MaterialButton pickBtn;
    private Context context;

    public TruEnumDataView(Context context, EnumInstance instance) {
        super(context, instance);
        this.context = context;
    }

    @Override
    protected Object getSelectedObject() {
       /* if (selectedPosition < 0)
            return "null";*/
        return instance.getEnumVals().get(selectedPosition);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tru_enum_data_view;
    }

    @Override
    protected void setInstanceData() {
        ((MaterialButton) mView.findViewById(R.id.pick_item_btn)).setText(instance.getPresentationTitle());
        if (selectedPosition >= 0 && instance.enumExists()) {
            String choosedItemTitle = "";
            if (instance.getEnumDisplayedNames().size() > 0)
                choosedItemTitle = String.valueOf(instance.getEnumDisplayedNames().get(selectedPosition));

            ((MaterialButton) mView.findViewById(R.id.pick_item_btn)).setText(choosedItemTitle);
        }
    }

    @Override
    protected void onViewCreated() {
        pickBtn = mView.findViewById(R.id.pick_item_btn);
        setInstanceData();
        setButtonClickListener();
        super.onViewCreated();
    }

    private void setButtonClickListener() {
        if (!instance.enumExists())
            pickBtn.setOnClickListener(getLoadItemsAction());
        else
            pickBtn.setOnClickListener((v) -> showChooserDialogAction());
    }

    private View.OnClickListener getLoadItemsAction() {
        return (v) -> {
            FormContract formActivity = getFormContract(v);
            if (formActivity != null) {
                DataInstance dataInstance = instance.getDataInstance();
                formActivity.onRequestData(getDataLoadedListener(), dataInstance.getIdentifierColumn(), dataInstance.getNames(), dataInstance.getUrl());
            }
        };
    }

    @NonNull
    private TruConsumer<ArrayList<Pair<Object, String>>> getDataLoadedListener() {
        return (pairArrayList) -> {
            ArrayList<Object> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            for (Pair<Object, String> pair : pairArrayList) {
                ids.add(pair.first);
                names.add(pair.second);
            }
            instance.setEnumVals(ids);
            instance.setEnumNames(names);
            if (!hasConstValue()) {
                setButtonClickListener();
                showChooserDialogAction();
            } else {
                setNonEditableValues(getItemNameForItemValue());
            }
        };
    }

    private boolean hasConstValue() {
        return instance.getConstItem() != null;
    }

    private Object getItemNameForItemValue() {
        int valIdx;
        if (instance.getEnumVals().size() > 0 && instance.getEnumVals().get(0) instanceof String) {
            valIdx = instance.getEnumVals().indexOf(instance.getConstItem());
        } else
            valIdx = instance.getEnumVals().indexOf(Double.parseDouble(instance.getConstItem().toString()));
        if (valIdx >= 0)
            return instance.getEnumNames().get(valIdx);
        else
            return instance.getConstItem(); //to pervent any unpredictable crashes
    }

    public void showChooserDialogAction() {
        String[] displayedNames = new String[2];
        displayedNames[0] = "123";
        displayedNames[1] = "1234";


        new MaterialAlertDialogBuilder(mContext)
                .setTitle(instance.getPresentationTitle())
                .setSingleChoiceItems(displayedNames, 0, null)
                .setPositiveButton(context.getString(R.string.ok), (dialog, whichButton) -> {
                    selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (valueChangedListener != null) {
                        valueChangedListener.onEnumValueChanged(instance.getKey(), instance.getEnumVals().get(selectedPosition));
                    }
                    setInstanceData();
                })
                .show();
    }

    @Override
    protected void setNonEditableValues(Object constItem) {
        if (pickBtn.isEnabled())
            pickBtn.performClick();
        String constStr = String.valueOf(constItem);
        if (TextUtils.isEmpty(constStr))
            pickBtn.setText(context.getString(R.string.non_selected));
        else
            pickBtn.setText(constStr.toString());
        pickBtn.setEnabled(false);
    }
}
