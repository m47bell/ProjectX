package charlyn23.c4q.nyc.projectx;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseUser;

import java.io.FileNotFoundException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "c4q.nyc.projectx";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int RC_SIGN_IN = 0;
    private static final String PROFILE_IMAGE = "profileImage";
    private static final String LOGGED_IN_GOOGLE = "isLoggedInGoogle";
    private static final String SHARED_PREFERENCE = "sharedPreference";
    private static final String LOGGED_IN = "isLoggedIn";
    private boolean isResolving = false;
    private boolean shouldResolve = false;
    private View view;
    private CircleImageView profileImage;
    private GoogleApiClient googleLogInClient;
    private boolean isLoggedIn_Google = false;
    private SharedPreferences preferences;

    public ProfileFragment(GoogleApiClient googleLogInClient) {
        this.googleLogInClient = googleLogInClient;
    }

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        preferences = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        isLoggedIn_Google = preferences.getBoolean(MainActivity.LOGGED_IN_GOOGLE, false);
        profileImage = (CircleImageView) view.findViewById(R.id.profile_image);
        setProfileImage();

        if (isLoggedIn_Google) {
            googleLogInClient.connect();
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileImage();
            }
        });

        ListView shame_list = (ListView) view.findViewById(R.id.shame_list);
        // IF list == 0, print "You haven't submitted any shames yet!"

        Button logout = (Button) view.findViewById(R.id.log_out);
        logout.setOnClickListener(logoutClick);
        return view;
    }

    // sets the user's image as a profile picture
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == MainActivity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            profileImage.setImageURI(selectedImage);

            //starts an intentService to save the new picture in a file
            String imagePath = selectedImage.toString();
            Intent intent = new Intent(getActivity(), PictureService.class);
            intent.putExtra(PROFILE_IMAGE, imagePath);
            getActivity().startService(intent);
        }
    }

    // brings up the photo gallery and other resources to choose a picture
    private void changeProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //sets and ImageView of the profile picture to the previously saved image
    private void setProfileImage() {
        Bitmap bm = PictureUtil.loadFromCacheFile();
        if (bm != null) {
            profileImage.setImageBitmap(bm);
        } else {
            //TODO: put default profile image
            profileImage.setImageResource(R.drawable.map);
        }
    }

    View.OnClickListener logoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ParseUser user = ParseUser.getCurrentUser();
            user.logOut();

            if (googleLogInClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(googleLogInClient);
                googleLogInClient.disconnect();
            }

            if (googleLogInClient.isConnected()) {
                Log.d("LOG OUT PELASE", "CLIENT WAS CONNECTED");
                Plus.AccountApi.clearDefaultAccount(googleLogInClient);
                googleLogInClient.disconnect();
            }

            SharedPreferences preferences = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(MainActivity.LOGGED_IN, false).apply();
            editor.putBoolean(MainActivity.LOGGED_IN_GOOGLE, false).apply();
            Toast.makeText(view.getContext(), getString(R.string.log_out_toast), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(view.getContext(), MainActivity.class);
            startActivity(intent);
        }
    };

    //saves profile image in the background
    public static class PictureService extends IntentService {

        public PictureService() {
            super("pictureService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String selectedImage = intent.getStringExtra(PROFILE_IMAGE);
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(selectedImage)));
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Image uri is not received or recognized");
            }
            PictureUtil.saveToCacheFile(bitmap);
        }
    }
}

