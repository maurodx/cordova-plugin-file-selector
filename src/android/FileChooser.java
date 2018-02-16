package com.megster.cordova;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

		    String displayName = null;
		    String uriString = uri.toString();
		    String mimeType = null;
		    String extension = null;

		    if (uriString.startsWith("content://")) {
			Cursor cursor = null;
			try {
			    cursor = cordova.getActivity().getContentResolver().query(uri, null, null, null, null);
			    if (cursor != null && cursor.moveToFirst()) {
				displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
			    }
			    mimeType = cordova.getActivity().getContentResolver().getType(uri);
			} finally {
			    cursor.close();
			}
		    } else if (uriString.startsWith("file://")) {
			displayName = new File(uriString).getName();
			String parts[]=uriString.split("\\.");
			String ext=parts[parts.length-1];
			if (ext != null) {
			    MimeTypeMap mime = MimeTypeMap.getSingleton();
			    mimeType = mime.getMimeTypeFromExtension(ext);
			}
		    }

		    if (mimeType != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			extension = mime.getExtensionFromMimeType(mimeType);
		    }

		    if (uriString.startsWith("content://com.android.gallery3d.provider"))  {
			//Use the com.google provider, not the com.android provider.
			uriString = uriString.replace("com.android.gallery3d","com.google.android.gallery3d");
		    }

		    if (uriString.startsWith("content://com.google.android.gallery3d") || uriString.startsWith("content://com.sec.android.gallery3d")) {
			ContextWrapper cw = new ContextWrapper(cordova.getActivity());
			File mypath=new File(cw.getCacheDir(),displayName);
			FileOutputStream fos = null;
			InputStream input = null;
			try {
			    fos = new FileOutputStream(mypath);
			    input = cordova.getActivity().getContentResolver().openInputStream(uri);
			    Bitmap bitmap = BitmapFactory.decodeStream(input);
			    if(bitmap != null){
				if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)){
				    uriString = mypath.toURI().toString();
				}
			    }
			    fos.close();
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }

		    try{
			JSONObject fileData = new JSONObject();
			fileData.put("uri",uriString);
			fileData.put("name",displayName);
			fileData.put("mime_type",mimeType);
			fileData.put("extension",extension);
			callback.success(fileData);
		    } catch(JSONException e) {
			callback.error("JSON Object not supported");
		    }

                } else {

                    callback.error("File uri was null");

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                // TODO NO_RESULT or error callback?
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);

            } else {

                callback.error(resultCode);
            }
        }
    }
}
