package com.example.firestoredatabaseapplication

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // creating variables for our edit text
    private lateinit var courseNameEdt: EditText // creating variables for our edit text
    private lateinit var courseDurationEdt: EditText  // creating variables for our edit text
    private lateinit var courseDescriptionEdt: EditText

    // creating variable for button
    private lateinit var submitCourseBtn: Button

    private lateinit var fetchCourseBtn: Button

    // creating a strings for storing
    // our values from edittext fields.
    private var courseName: String? = null  // creating a strings for storing

    // our values from edittext fields.
    private var courseDuration: String? = null  // creating a strings for storing

    // our values from edittext fields.
    private var courseDescription: String? = null

    // creating a variable
    // for firebasefirestore.
    private lateinit var db: FirebaseFirestore
    private lateinit var dbCourses: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // getting our instance
        // from Firebase Firestore.
        db = FirebaseFirestore.getInstance()
        dbCourses = db.collection("Courses")

        // initializing our edittext and buttons
        courseNameEdt = findViewById(R.id.idEdtCourseName)
        courseDescriptionEdt = findViewById(R.id.idEdtCourseDescription)
        courseDurationEdt = findViewById(R.id.idEdtCourseDuration)
        submitCourseBtn = findViewById(R.id.idBtnSubmitCourse)
        fetchCourseBtn = findViewById(R.id.idBtnFetchCourse)

        // adding on click listener for button
        submitCourseBtn.setOnClickListener {
            // getting data from edittext fields.
            courseName = courseNameEdt.text.toString()
            courseDescription = courseDescriptionEdt.text.toString()
            courseDuration = courseDurationEdt.text.toString()

            // validating the text fields if empty or not.
            if (TextUtils.isEmpty(courseName)) {
                courseNameEdt.error = "Please enter Course Name"
            } else if (TextUtils.isEmpty(courseDescription)) {
                courseDescriptionEdt.error = "Please enter Course Description"
            } else if (TextUtils.isEmpty(courseDuration)) {
                courseDurationEdt.error = "Please enter Course Duration"
            } else {
                // calling method to add data to Firebase Firestore.
                addDataToFirestore(courseName, courseDescription, courseDuration)
            }
        }

        fetchCourseBtn.setOnClickListener {
            dbCourses.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val courses: Courses = document.toObject(Courses::class.java)
                        Log.d(TAG, "Firestore addOnSuccessListener $courses")
                        Log.d(
                            TAG,
                            "Firestore addOnSuccessListener ${document.id} => ${document.data}"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        dbCourses.addSnapshotListener(object : EventListener<QuerySnapshot?> {
            override fun onEvent(
                @Nullable snapshots: QuerySnapshot?,
                @Nullable e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    Log.w(TAG, "Listen:error " + e.message, e)
                    return
                }
                for (dc in snapshots?.documentChanges!!) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "New city ADDED: " + dc.document.data)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Modified: \n" + dc.document.data,
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d(TAG, "Modified city MODIFIED: " + dc.document.data)
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "Removed city REMOVED: " + dc.document.data)
                        }
                    }
                }
            }
        })

    }

    private fun addDataToFirestore(
        courseName: String?,
        courseDescription: String?,
        courseDuration: String?
    ) {

        // creating a collection reference
        // for our Firebase Firetore database.
        //val dbCourses = db.collection("Courses")

        // adding our data to our courses object class.
        val courses = Courses(courseName, courseDescription, courseDuration)

        // below method is use to add data to Firebase Firestore.
        dbCourses.add(courses).addOnSuccessListener { // after the data addition is successful
            // we are displaying a success toast message.
            Toast.makeText(
                this@MainActivity,
                "Your Course has been added to Firebase Firestore",
                Toast.LENGTH_SHORT
            ).show()
        }
            .addOnFailureListener { e -> // this method is called when the data addition process is failed.
                // displaying a toast message when data addition is failed.
                Toast.makeText(this@MainActivity, "Fail to add course \n$e", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}