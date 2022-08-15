package com.dyltech.foodrecipesqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_recipe.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class RecipeFragment : Fragment() {
    var selectImage: Uri? = null
    var selectBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            save(it)
        }
        imageView.setOnClickListener {
            imageSelect(it)
        }

        arguments?.let {
            var incomingInfo = RecipeFragmentArgs.fromBundle(it).info

            if (incomingInfo.equals("menudengeldim")) {
                foodNameText.setText("")
                foodStuffText.setText("")
                button.visibility = View.VISIBLE

                val imageSelectBackGround= BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                imageView.setImageBitmap(imageSelectBackGround)

            } else {
                button.visibility = View.INVISIBLE

                val selectedId=RecipeFragmentArgs.fromBundle(it).id

                context?.let {
                    try {

                        val db= it.openOrCreateDatabase("Foods",Context.MODE_PRIVATE,null)
                        val cursor =  db.rawQuery( "SELECT * FROM foods WHERE id = ?", arrayOf(selectedId.toString()))
                        val foodNameIndex = cursor.getColumnIndex("foodname")
                        val foodStuffIndex = cursor.getColumnIndex("foodstuff")
                        val foodImage=cursor.getColumnIndex("image")

                        while (cursor.moveToNext()){
                            foodNameText.setText(cursor.getString(foodNameIndex))
                            foodStuffText.setText(cursor.getString(foodStuffIndex))

                            val byteArray=cursor.getBlob(foodImage)
                            val bitmap =BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                            imageView.setImageBitmap(bitmap)

                        }
                        cursor.close()

                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun save(view: View) {

        val foodName = foodNameText.text.toString()
        val foodStuff = foodStuffText.text.toString()

        if (selectBitmap != null) {
            val smallBitmap = smallBitmapCreate(selectBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS  foods (id INTEGER PRIMARY KEY,foodname VARCHAR, foodstuff VARCHAR, image BLOB)")
                    val sqlString = "INSERT INTO foods (foodname,foodstuff,image) VALUES(?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, foodName)
                    statement.bindString(2, foodStuff)
                    statement.bindBlob(3, byteArray)
                    statement.execute()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val action = RecipeFragmentDirections.actionRecipeFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)


        }


    }

    private fun imageSelect(view: View) {

        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                val mediaIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(mediaIntent, 2)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mediaIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(mediaIntent, 2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            selectImage = data.data

            try {
                context?.let {
                    if (selectImage != null) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(it.contentResolver, selectImage!!)
                            selectBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(selectBitmap)
                        } else {
                            selectBitmap =
                                MediaStore.Images.Media.getBitmap(it.contentResolver, selectImage)
                            imageView.setImageBitmap(selectBitmap)
                        }

                    }
                }

            } catch (e: Exception) {

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun smallBitmapCreate(userChoiceBitmap: Bitmap, maximumSize: Int): Bitmap {
        var width = userChoiceBitmap.width
        var height = userChoiceBitmap.height

        val ratioBitmap: Double = width.toDouble() / height.toDouble()

        if (ratioBitmap > 1) {
            width = maximumSize
            val shortHeight = width / ratioBitmap
            height = shortHeight.toInt()
        } else {
            height = maximumSize
            val shortWidht = height * ratioBitmap
            width = shortWidht.toInt()
        }

        return Bitmap.createScaledBitmap(userChoiceBitmap, width, height, true)
    }


}