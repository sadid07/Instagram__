package com.example.instagramclone

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.parse.*
import java.io.File
import java.util.*

/*
 let user create a post by taking a photo with their camera
 */


class MainActivity : AppCompatActivity() {
    val APP_TAG = "MyCustomApp"
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. setting the description of the post
        // 2. a button to launch the camera to take the picture
        // 3. an ImageView to show the picture the user has taken
        // 4. a button to save and send the post to our Parse server

        findViewById<Button>(R.id.btnTSubmit).setOnClickListener {
            // send post to server without an image
            // get the description that they have inputted
            val description = findViewById<EditText>(R.id.et_description).text.toString()
            val user = ParseUser.getCurrentUser()
            if (photoFile != null) {
                submitPost(description, user, photoFile!!)
            } else {
                // TODO: Print error log message
                // TODO: Show a toast message to let user know
            }

        }

        findViewById<Button>(R.id.btnTakePicture).setOnClickListener {
            // launch camera to let user take picture
            onLaunchCamera()
        }
//        queryPost()

        // loggin the user out
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logoutUser()
            if (ParseUser.getCurrentUser() == null) {
                goToLoginActivity()
            }
        }
    }

    // send a post object to our parse server
    private fun submitPost(description: String, user: ParseUser, file: File) {
        // create a post object to send to server
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground { e ->
            if (e != null) {
                // somethiong has went wrong
                Log.e(TAG, "Erro while saving post")
                e.printStackTrace()
                // show toast to tell user something went wrong with saving post

            } else {
                Log.i(TAG, "Succeessfully save post")
                // TODO: resetting the EditText field to be empty
                // TODO: reset the ImageView to empty
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                val ivPreview: ImageView = findViewById(R.id.iv_uploadImage)
                ivPreview.setImageBitmap(takenImage)
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName)

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        if (photoFile != null) {
            val fileProvider: Uri =
                FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
    }

    private fun getPhotoFileUri(fileName: String): File? {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    fun logoutUser() {
        ParseUser.logOut()
    }

    // query for all posts in our server
    fun queryPost() {
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        // find all post objects
        query.findInBackground(object: FindCallback<Post> {
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null) {
                    // something has went wrong
                    Log.e(TAG, "error fetching posts")
                } else {
                    if (posts != null) {
                        for (post in posts) {
                            Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser()?.username)
                        }
                    }
                }
            }
        })
    }

    fun goToLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}