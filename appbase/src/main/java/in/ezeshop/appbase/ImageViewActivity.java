package in.ezeshop.appbase;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import in.ezeshop.appbase.utilities.LogMy;

/**
 * Created by adgangwa on 06-10-2017.
 */

public class ImageViewActivity extends AppCompatActivity {

    private static final String TAG = "BaseApp-ImageViewActivity";

    // constants used to pass extra data in the intent
    public static final String INTENT_EXTRA_URI = "ImageUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        try {
            Uri uri = (Uri)getIntent().getParcelableExtra(INTENT_EXTRA_URI);
            if(uri!=null) {
                LogMy.d(TAG,uri.toString());

                final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);
                imageView.setImage(ImageSource.uri(uri));
            } else {
                LogMy.e(TAG,"URI not present");
            }
        }catch (Exception e) {
            LogMy.e(TAG,"Exception in ImageViewActivity",e);
        }
    }
}


