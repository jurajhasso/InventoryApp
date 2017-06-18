package com.example.android.inventoryapp;

/**
 * Created by XY on 12.6.2017.
 */

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.content.CursorLoader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.inventoryapp.data.ItemProvider.LOG_TAG;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 0;

    private Uri mCurrentItemUri;

    private EditText mNameEditText;

    private EditText mDescriptionEditText;

    private Button mQuantityPlus;

    private Button mQuantityMinus;

    private int currentQuantity;

    private EditText mCurrentQuantity;

    private EditText mPriceEditText;

    private Button mChooseImage;

    private Button mOrderFromSupplierButton;

    public Uri mImageUri;

    public ImageView mCurrentImage;

    private boolean mItemHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_pet));

            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit_pet));

            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mOrderFromSupplierButton = (Button) findViewById(R.id.order_button);

        mOrderFromSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                composeEmail(getString(R.string.emailSubject));
            }
        });

        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mCurrentQuantity = (EditText) findViewById(R.id.quantity);
        mChooseImage = (Button) findViewById(R.id.choose_image);
        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        mCurrentImage = (ImageView) findViewById(R.id.current_image);

        mQuantityPlus = (Button) findViewById(R.id.quantity_plus);
        mQuantityPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentQuantity = (EditText) findViewById(R.id.quantity);
                String currentQuantityString = mCurrentQuantity.getText().toString();
                int currentQuantity = 0;
                if (!TextUtils.isEmpty(currentQuantityString)) {
                    currentQuantity = Integer.parseInt(currentQuantityString);
                }
                currentQuantity = currentQuantity + 1;
                currentQuantityString = Integer.toString(currentQuantity);
                mCurrentQuantity.setText(String.valueOf(currentQuantityString));
            }
        });

        mQuantityMinus = (Button) findViewById(R.id.quantity_minus);
        mQuantityMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentQuantity = (EditText) findViewById(R.id.quantity);
                String currentQuantityString = mCurrentQuantity.getText().toString();
                int currentQuantity = 0;
                if (!TextUtils.isEmpty(currentQuantityString)) {
                    currentQuantity = Integer.parseInt(currentQuantityString);
                }
                if (currentQuantity > 0) {
                    currentQuantity = currentQuantity - 1;
                    currentQuantityString = Integer.toString(currentQuantity);
                    mCurrentQuantity.setText(String.valueOf(currentQuantityString));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.invalid_quantity, Toast.LENGTH_SHORT).show();
                }
            }
        });


        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityPlus.setOnTouchListener(mTouchListener);
        mQuantityMinus.setOnTouchListener(mTouchListener);
        mChooseImage.setOnTouchListener(mTouchListener);
    }

    private boolean checkBeforeSaving(Context context) {
        if (mNameEditText.getText().toString().trim().equals("")) {
            Toast.makeText(context, R.string.name_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (mCurrentQuantity.getText().toString().trim().equals("")) {
            Toast.makeText(context, R.string.quantity_required, Toast.LENGTH_SHORT).show();
            return false;

        }
        else if (mPriceEditText.getText().toString().equals("")){
            Toast.makeText(context, R.string.price_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        else if (mCurrentImage.getDrawable() == null){
            Toast.makeText(context, R.string.image_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    private void saveItem() {

            String nameString = mNameEditText.getText().toString().trim();
            String descriptionString = mDescriptionEditText.getText().toString().trim();
            String priceString = mPriceEditText.getText().toString().trim();
            String quantityString = mCurrentQuantity.getText().toString();
            String imageUriString = mImageUri.toString().trim();

            if (mCurrentItemUri == null &&
                    TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)
                    && TextUtils.isEmpty(imageUriString)) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
            values.put(ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
            values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityString);
            values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageUriString);

            int price = 0;
            if (!TextUtils.isEmpty(priceString)) {
                price = Integer.parseInt(priceString);
            }
            values.put(ItemEntry.COLUMN_ITEM_PRICE, price);

            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

            if (mCurrentItemUri == null) {

                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

                if (newUri == null) {

                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {

                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                if (rowsAffected == 0) {

                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
    }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){

            getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
        }

        @Override
        public boolean onPrepareOptionsMenu (Menu menu){
            super.onPrepareOptionsMenu(menu);
            if (mCurrentItemUri == null) {
                MenuItem menuItem = menu.findItem(R.id.action_delete);
                menuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){

            switch (item.getItemId()) {
                case R.id.action_save:
                    checkBeforeSaving(getApplicationContext());
                    if (checkBeforeSaving(getApplicationContext())) {
                        saveItem();
                        finish();
                    }
                    else
                    {
                        return true;
                    }
                    return true;
                case R.id.action_delete:
                    showDeleteConfirmationDialog();
                    return true;
                case android.R.id.home:
                    if (!mItemHasChanged) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        return true;
                    }

                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed () {
            if (!mItemHasChanged) {
                super.onBackPressed();
                return;
            }

            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
        }

    public void composeEmail(String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void openImageSelector() {

        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.picture_select)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());
                mCurrentImage.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mCurrentImage.getWidth();
        int targetH = mCurrentImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, getString(R.string.image_load_failed), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, getString(R.string.image_load_failed), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_DESCRIPTION,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            currentQuantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);
            Log.i(LOG_TAG, "Uri in EditorActivity: " + imageUriString);
            mImageUri = Uri.parse(imageUriString);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mCurrentQuantity.setText(Integer.toString(currentQuantity));
            mPriceEditText.setText(Integer.toString(price));
            mCurrentImage.setImageBitmap(getBitmapFromUri(mImageUri));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mCurrentQuantity.setText("");
        mPriceEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {

        if (mCurrentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {

                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}