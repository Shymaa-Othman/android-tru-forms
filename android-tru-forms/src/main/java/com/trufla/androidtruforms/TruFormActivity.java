package com.trufla.androidtruforms;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.gson.Gson;
import com.trufla.androidtruforms.databinding.ActivityTruFormBinding;
import com.trufla.androidtruforms.interfaces.TruConsumer;
import com.trufla.androidtruforms.truviews.TruFormView;
import com.trufla.androidtruforms.utils.BitmapUtils;
import com.trufla.androidtruforms.utils.TruUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by ohefny on 8/13/18.
 */

public class TruFormActivity extends AppCompatActivity {
    private static final String JSON_KEY = "JSON_KEY";
    private static final int IMAGE_PICKER_CODE = 505;
    private static SchemaBuilder sSchemaBuilder;
    private TruFormView truFormView;
    TruConsumer<String> mPickedImageListener;

    public static void startActivityForFormResult(Activity context, String jsonStr, SchemaBuilder schemaBuilder) {
        Intent intent = new Intent(context, TruFormActivity.class);
        sSchemaBuilder = schemaBuilder;
        intent.putExtra(JSON_KEY, jsonStr);
        context.startActivityForResult(intent, SchemaBuilder.REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTruFormBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_tru_form);
        try {
            truFormView = sSchemaBuilder.buildSchemaView(getIntent().getExtras().getString(JSON_KEY), this);
            View formView = truFormView.build();
            binding.formContainer.addView(formView);
            binding.setFormView(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Unable to create the form ... please check the schema", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    public void onSubmitClicked() {
        if (!isValidData())
            return;
        Toast.makeText(this, "submitted", Toast.LENGTH_SHORT).show();
        String result = truFormView.getInputtedData();
        Intent intent = new Intent();
        intent.putExtra(SchemaBuilder.RESULT_DATA_KEY, result);
        setResult(RESULT_OK, intent);
        finish();

    }

    public void openImagePicker(TruConsumer<String> pickedImageListener) {
        this.mPickedImageListener = pickedImageListener;
        ImagePicker.create(this).single() // single mode
                // Activity or Fragment
                .start(IMAGE_PICKER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_CODE) {
            // Get a list of picked images
            List<Image> images = ImagePicker.getImages(data);
            // or get a single image only
            if (images == null || images.size() == 0)
                return;
            Image image = ImagePicker.getImages(data).get(0);
            String path=image.getPath();
            if (!TruUtils.isEmpty(path) && mPickedImageListener != null) {
                mPickedImageListener.accept(path);
            }

        }
    }

    private boolean isValidData() {
        return true;
    }

}