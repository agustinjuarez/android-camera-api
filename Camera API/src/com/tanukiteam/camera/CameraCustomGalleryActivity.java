package com.tanukiteam.camera;

import com.tanukiteam.camera.CameraAPIActivity.Mode;
import com.tanukiteam.camera.widget.CancelButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
 
public class CameraCustomGalleryActivity extends Activity {
    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    private ImageAdapter imageAdapter;
    public static ImageView imgView;
    private Uri u;
    private CancelButton btnCancel;
    private Mode status;
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get the status
    	Intent intent = getIntent();
    	status = (Mode) intent.getExtras().get("status");
        
        setContentView(R.layout.gallery);
        btnCancel = (CancelButton)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	cancelFunction();
	        }
	    });
        
        imgView = new ImageView(this);
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        this.count = imagecursor.getCount();
        this.thumbnails = new Bitmap[this.count];
        this.arrPath = new String[this.count];
        this.thumbnailsselection = new boolean[this.count];
        for (int i = 0; i < this.count; i++) {
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                    getApplicationContext().getContentResolver(), id,
                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
            arrPath[i]= imagecursor.getString(dataColumnIndex);
        }
        GridView imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);
        imagecursor.close();
 
        final Button selectBtn = (Button) findViewById(R.id.btnPhotos);
        
        selectBtn.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                final int len = thumbnailsselection.length;
                int cnt = 0;
                String selectImages = "";
                for (int i =0; i<len; i++)
                {
                    if (thumbnailsselection[i]){
                        cnt++;
                        selectImages = selectImages + arrPath[i] + "|";
                    }
                }
                if (cnt == 0){
                    Toast.makeText(getApplicationContext(),
                            "Please select at least one image",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "You've selected Total " + cnt + " image(s).",
                            Toast.LENGTH_LONG).show();
                    Log.d("SelectedImages", selectImages);
                }
            }
        });
    }
 
    /**
     * Adapter class 
     */
    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
 
        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
 
        public int getCount() {
            return count;
        }
 
        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageview.setId(position);
            
            /**
             * When the user clicks on an image this comes back to the CameraAPIActivity
             * and the picture selected will be displayed
             */
            holder.imageview.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int id = v.getId();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + arrPath[id]), "image/*");
                     u = intent.getData();
                    goBackActivity(arrPath[id]);
                }
            });
            holder.imageview.setImageBitmap(thumbnails[position]);
            holder.id = position;
            return convertView;
        }
    }
    
    /**
     * @author ajuarez@tanukiteam.com
     * Inner class
     */
    class ViewHolder {
        ImageView imageview;
        int id;
    }
    
    /**
     * @author ajuarez@tanukiteam.com
     * @return void
     * @param String
     * This method goes back to the CameraAPIActivity activity 
     * sending the selected picture.
     */
    private void goBackActivity(String s){
    	Intent intent = new Intent(this,CameraAPIActivity.class);  
        intent.putExtra("picture",s);
        startActivity(intent);
        this.finish();
    }
    
    /**
     * @author ajuarez@tanukiteam.com
     * @return void
     * This method goes back to the CameraAPIActivity activity.
     */
    private void cancelFunction(){
    	Intent intent = new Intent(this,CameraAPIActivity.class);  
    	intent.putExtra("status",status);
        startActivity(intent);
        this.finish();
    }
}
