/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.posenet
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.graphics.Typeface
import kotlinx.android.synthetic.main.endingdialog.view.*

import kotlinx.android.synthetic.main.activity_posenet.*
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.core.app.ActivityCompat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat

import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import params.com.stepprogressview.StepProgressView
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.widget.Toast
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.tensorflow.lite.examples.posenet.lib.BodyPart
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.lite.examples.posenet.lib.Posenet
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin




class PosenetActivity :
  Fragment(),
  ActivityCompat.OnRequestPermissionsResultCallback {
  private var stage=1
  private  var showscore:Float =0f
   var isshow:Boolean = false
  /** List of body joints that should be connected.    */
  private val bodyJoints = listOf(
    Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
    Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
    Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
    Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
    Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
    Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
    Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
    Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
  )



  /** Threshold for confidence score. */
  private val minConfidence = 0.45

  /** Radius of circle used to draw keypoints.  */
  private val circleRadius = 8.0f

  /** Paint class holds the style and color information to draw geometries,text and bitmaps. */
  private var paint = Paint()

  /** A shape for extracting frame data.   */
  private val PREVIEW_WIDTH = 640
  private val PREVIEW_HEIGHT = 480

  /** An object for the Posenet library.    */
  private lateinit var posenet: Posenet

  /** ID of the current [CameraDevice].   */
  private var cameraId: String? = null

  /** A [SurfaceView] for camera preview.   */
  private var surfaceView: SurfaceView? = null

  private var imageView1 : ImageView? = null
  private var imageView2 : ImageView? = null
  private var imageView3: ImageView? = null
  private var imageView4 : ImageView? = null
  private var imageView5 : ImageView? = null
  private var stepProgressView: StepProgressView? = null

  /** A [CameraCaptureSession] for camera preview.   */
  private var captureSession: CameraCaptureSession? = null

  /** A reference to the opened [CameraDevice].    */
  private var cameraDevice: CameraDevice? = null

  /** The [android.util.Size] of camera preview.  */
  private var previewSize: Size? = null

  /** The [android.util.Size.getWidth] of camera preview. */
  private var previewWidth = 0

  /** The [android.util.Size.getHeight] of camera preview.  */
  private var previewHeight = 0

  /** A counter to keep count of total frames.  */
  private var frameCounter = 0

  /** An IntArray to save image data in ARGB8888 format  */
  private lateinit var rgbBytes: IntArray

  /** A ByteArray to save image data in YUV format  */
  private var yuvBytes = arrayOfNulls<ByteArray>(3)

  /** An additional thread for running tasks that shouldn't block the UI.   */
  private var backgroundThread: HandlerThread? = null

  /** A [Handler] for running tasks in the background.    */
  private var backgroundHandler: Handler? = null

  /** An [ImageReader] that handles preview frame capture.   */
  private var imageReader: ImageReader? = null

  /** [CaptureRequest.Builder] for the camera preview   */
  private var previewRequestBuilder: CaptureRequest.Builder? = null

  /** [CaptureRequest] generated by [.previewRequestBuilder   */
  private var previewRequest: CaptureRequest? = null

  /** A [Semaphore] to prevent the app from exiting before closing the camera.    */
  private val cameraOpenCloseLock = Semaphore(1)

  /** Whether the current camera device supports Flash or not.    */
  private var flashSupported = false

  /** Orientation of the camera sensor.   */
  private var sensorOrientation: Int? = null

  /** Abstract interface to someone holding a display surface.    */
  private var surfaceHolder: SurfaceHolder? = null



  //private var stage=4

  private fun getcos(v1:Float,v2:Float, v3:Float, v4:Float):Float{
    var cos =
      (v1*v3+v2*v4) / (2 * sqrt(v1 * v1+v2*v2) * sqrt(v3*v3+v4*v4))
    var simil = 1 - cos
    return simil
  }

  /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.   */
  private val stateCallback = object : CameraDevice.StateCallback() {

    override fun onOpened(cameraDevice: CameraDevice) {
      cameraOpenCloseLock.release()
      this@PosenetActivity.cameraDevice = cameraDevice
      createCameraPreviewSession()
    }

    override fun onDisconnected(cameraDevice: CameraDevice) {
      cameraOpenCloseLock.release()
      cameraDevice.close()
      this@PosenetActivity.cameraDevice = null
    }

    override fun onError(cameraDevice: CameraDevice, error: Int) {
      onDisconnected(cameraDevice)
      this@PosenetActivity.activity?.finish()
    }
  }

  /**
   * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
   */
  private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureProgressed(
      session: CameraCaptureSession,
      request: CaptureRequest,
      partialResult: CaptureResult
    ) {
    }

    override fun onCaptureCompleted(
      session: CameraCaptureSession,
      request: CaptureRequest,
      result: TotalCaptureResult
    ) {
    }
  }

  /**
   * Shows a [Toast] on the UI thread.
   *
   * @param text The message to show
   */
  private fun showToast(text: String) {
    val activity = activity
    activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?): View? {



    return inflater.inflate(R.layout.activity_posenet, container, false)

  }



  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    //super.onViewCreated(view, savedInstanceState)
    surfaceView = view.findViewById(R.id.surfaceView)
    surfaceHolder = surfaceView!!.holder
    imageView1= view.findViewById(R.id.stage1)
    imageView2= view.findViewById(R.id.stage2)
    imageView3= view.findViewById(R.id.stage3)
    imageView4= view.findViewById(R.id.stage4)
    imageView5= view.findViewById(R.id.stage5)
    stepProgressView=view.findViewById(R.id.score)



  }
  private fun showtoast(){

    val layoutInflater= this.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout=layoutInflater.inflate(R.layout.endingdialog,null)
    val toast=Toast(context?.applicationContext)
    toast.setGravity(Gravity.CENTER,0,0)
    toast.duration=Toast.LENGTH_LONG

    toast.view=layout
    toast.show()
  }


  override fun onResume() {
    super.onResume()
    startBackgroundThread()
  }

  override fun onStart() {
    super.onStart()
    openCamera()
    posenet = Posenet(this.context!!)
  }

  override fun onPause() {
    closeCamera()
    stopBackgroundThread()
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    posenet.close()
  }

  private fun requestCameraPermission() {
    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
    } else {
      requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (requestCode == REQUEST_CAMERA_PERMISSION) {
      if (allPermissionsGranted(grantResults)) {
        ErrorDialog.newInstance(getString(R.string.request_permission))
          .show(childFragmentManager, FRAGMENT_DIALOG)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  private fun allPermissionsGranted(grantResults: IntArray) = grantResults.all {
    it == PackageManager.PERMISSION_GRANTED
  }

  /**
   * Sets up member variables related to camera.
   */
  private fun setUpCameraOutputs() {

    val activity = activity
    val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      for (cameraId in manager.cameraIdList) {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        // We don't use a front facing camera in this sample.
        val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (cameraDirection != null &&
          cameraDirection == CameraCharacteristics.LENS_FACING_BACK
        ) {
          continue
        }
        //에ㅎ


        previewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

        imageReader = ImageReader.newInstance(
          PREVIEW_WIDTH, PREVIEW_HEIGHT,
          ImageFormat.YUV_420_888, /*maxImages*/ 2
        )

        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        previewHeight = previewSize!!.height
        previewWidth = previewSize!!.width

        // Initialize the storage bitmaps once when the resolution is known.
        rgbBytes = IntArray(previewWidth * previewHeight)

        // Check if the flash is supported.
        flashSupported =
          characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

        this.cameraId = cameraId

        // We've found a viable camera and finished setting up member variables,
        // so we don't need to iterate through other available cameras.
        return
      }
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    } catch (e: NullPointerException) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
        .show(childFragmentManager, FRAGMENT_DIALOG)
    }
  }

  /**
   * Opens the camera specified by [PosenetActivity.cameraId].
   */
  private fun openCamera() {
    val permissionCamera = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
    if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
      requestCameraPermission()
    }
    setUpCameraOutputs()
    val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    try {
      // Wait for camera to open - 2.5 seconds is sufficient
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw RuntimeException("Time out waiting to lock camera opening.")
      }
      manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera opening.", e)
    }
  }

  /**
   * Closes the current [CameraDevice].
   */
  private fun closeCamera() {
    if (captureSession == null) {
      return
    }

    try {
      cameraOpenCloseLock.acquire()
      captureSession!!.close()
      captureSession = null
      cameraDevice!!.close()
      cameraDevice = null
      imageReader!!.close()
      imageReader = null
    } catch (e: InterruptedException) {
      throw RuntimeException("Interrupted while trying to lock camera closing.", e)
    } finally {
      cameraOpenCloseLock.release()
    }
  }

  /**
   * Starts a background thread and its [Handler].
   */
  private fun startBackgroundThread() {
    backgroundThread = HandlerThread("imageAvailableListener").also { it.start() }
    backgroundHandler = Handler(backgroundThread!!.looper)
  }

  /**
   * Stops the background thread and its [Handler].
   */
  private fun stopBackgroundThread() {
    backgroundThread?.quitSafely()
    try {
      backgroundThread?.join()
      backgroundThread = null
      backgroundHandler = null
    } catch (e: InterruptedException) {
      Log.e(TAG, e.toString())
    }
  }

  /** Fill the yuvBytes with data from image planes.   */
  private fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
    // Row stride is the total number of bytes occupied in memory by a row of an image.
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (i in planes.indices) {
      val buffer = planes[i].buffer
      if (yuvBytes[i] == null) {
        yuvBytes[i] = ByteArray(buffer.capacity())
      }
      buffer.get(yuvBytes[i]!!)
    }
  }
  private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
    val matrix = Matrix().apply { postScale(x, y, cx, cy) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
  }
  /** A [OnImageAvailableListener] to receive frames as they are available.  */
  private var imageAvailableListener = object : OnImageAvailableListener {
    override fun onImageAvailable(imageReader: ImageReader) {
      // We need wait until we have some size from onPreviewSizeChosen
      if (previewWidth == 0 || previewHeight == 0) {
        return
      }


      val image = imageReader.acquireLatestImage() ?: return
      fillBytes(image.planes, yuvBytes)

      ImageUtils.convertYUV420ToARGB8888(
        yuvBytes[0]!!,
        yuvBytes[1]!!,
        yuvBytes[2]!!,
        previewWidth,
        previewHeight,
        /*yRowStride=*/ image.planes[0].rowStride,
        /*uvRowStride=*/ image.planes[1].rowStride,
        /*uvPixelStride=*/ image.planes[1].pixelStride,
        rgbBytes
      )

      // Create bitmap from int array
      val imageBitmap = Bitmap.createBitmap(
        rgbBytes, previewWidth, previewHeight,
        Bitmap.Config.ARGB_8888
      )

      // Create rotated version for portrait display
      val rotateMatrix = Matrix()
      rotateMatrix.postRotate(270.0f)


      val rotatedBitmap = Bitmap.createBitmap(
        imageBitmap, 0, 0, previewWidth, previewHeight,
        rotateMatrix, true
      )
      image.close()
      val cx = rotatedBitmap.width / 2f
      val cy = rotatedBitmap.height / 2f
      val flippedBitmap = rotatedBitmap.flip(-1f, 1f, cx, cy)

      // Process an image for analysis in every 2 frames.
      frameCounter = (frameCounter + 1) % 2
      if (frameCounter == 0) {
        processImage(flippedBitmap)
      }

    }
  }

  /** Crop Bitmap to maintain aspect ratio of model input.   */
  private fun cropBitmap(bitmap: Bitmap): Bitmap {
    val bitmapRatio = bitmap.height.toFloat() / bitmap.width
    val modelInputRatio = MODEL_HEIGHT.toFloat() / MODEL_WIDTH
    var croppedBitmap = bitmap

    // Acceptable difference between the modelInputRatio and bitmapRatio to skip cropping.
    val maxDifference = 1e-5

    // Checks if the bitmap has similar aspect ratio as the required model input.
    when {
      abs(modelInputRatio - bitmapRatio) < maxDifference -> return croppedBitmap
      modelInputRatio < bitmapRatio -> {
        // New image is taller so we are height constrained.
        val cropHeight = bitmap.height - (bitmap.width.toFloat() / modelInputRatio)
        croppedBitmap = Bitmap.createBitmap(
          bitmap,
          0,
          (cropHeight / 2).toInt(),
          bitmap.width,
          (bitmap.height - cropHeight).toInt()
        )
      }
      else -> {
        val cropWidth = bitmap.width - (bitmap.height.toFloat() * modelInputRatio)
        croppedBitmap = Bitmap.createBitmap(
          bitmap,
          (cropWidth / 2).toInt(),
          0,
          (bitmap.width - cropWidth).toInt(),
          bitmap.height
        )
      }
    }
    return croppedBitmap
  }

  /** Set the paint color and size.    */
  private fun setPaint() {
    paint.color = Color.rgb(255, 165, 0)
    paint.textSize = 80.0f
    paint.strokeWidth = 8.0f
    paint.setTypeface( Typeface.createFromAsset(context?.assets, "nsbold.ttf"))

  }


  /** Draw bitmap on Canvas.   */
  private fun draw(canvas: Canvas, person: Person, bitmap: Bitmap) {
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    // Draw `bitmap` and `person` in square canvas.
    val rad=0.01745
    val screenWidth: Int
    val screenHeight: Int
    val left: Int
    val right: Int
    val top: Int
    val bottom: Int
    if (canvas.height > canvas.width) {
      screenWidth = canvas.width
      screenHeight = canvas.width
      left = 0
      top = (canvas.height - canvas.width) / 2
    } else {
      screenWidth = canvas.height
      screenHeight = canvas.height
      left = (canvas.width - canvas.height) / 2
      top = 0
    }

    right = left + screenWidth
    bottom = top + screenHeight
    /*크기알자
    print("left ")
    println(left.toString());
    print("r ")
    println(right.toString());
    print("screen w ")
    println(screenWidth.toString());
    print("screen Height ")
    println(screenHeight.toString());
    print("top ")
    println(top.toString());
    print("bottom ")
    println(bottom.toString());
    */
    setPaint()
    canvas.drawBitmap(
      bitmap,
      Rect(0, 0, bitmap.width, bitmap.height),
      Rect(left, top, right, bottom),
      paint
    )

    val widthRatio = screenWidth.toFloat() / MODEL_WIDTH
    val heightRatio = screenHeight.toFloat() / MODEL_HEIGHT

    // Draw key points over the image.
    for (keyPoint in person.keyPoints) {
      if (keyPoint.score > minConfidence) {
        val index=keyPoint.bodyPart
        val position = keyPoint.position
        val adjustedX: Float = position.x.toFloat() * widthRatio + left
        val adjustedY: Float = position.y.toFloat() * heightRatio + top
        canvas.drawCircle(adjustedX, adjustedY, circleRadius, paint)
        /*println(index)
        println(position.x.toFloat().toString())
        println(position.y.toFloat().toString())
        print(index)
        println(keyPoint.score.toString())*/

      }
    }




    for (line in bodyJoints) {
      var firstf=person.keyPoints[line.first.ordinal]
      var secondf=person.keyPoints[line.second.ordinal]
      if (
        (firstf.score > minConfidence) and
        (secondf.score > minConfidence)
      ) {
        canvas.drawLine(
          firstf.position.x.toFloat() * widthRatio + left,
          firstf.position.y.toFloat() * heightRatio + top,
          secondf.position.x.toFloat() * widthRatio + left,
          secondf.position.y.toFloat() * heightRatio + top,
          paint
        )
      }
    }
    // 팔 다리 벡터구하기
    if(person.score>0.6){
      //left arm
      val leftarm1x= person.keyPoints[7].position.x-person.keyPoints[5].position.x
      val leftarm1y= person.keyPoints[7].position.y-person.keyPoints[5].position.y
      val leftarm2x= person.keyPoints[9].position.x-person.keyPoints[7].position.x
      val leftarm2y= person.keyPoints[9].position.y-person.keyPoints[7].position.y

      //right arm
      val rightarm1x= person.keyPoints[8].position.x-person.keyPoints[6].position.x
      val rightarm1y= person.keyPoints[8].position.y-person.keyPoints[6].position.y
      val rightarm2x= person.keyPoints[10].position.x-person.keyPoints[8].position.x
      val rightarm2y= person.keyPoints[10].position.y-person.keyPoints[8].position.y

      //left leg
      val leftleg1x= person.keyPoints[13].position.x-person.keyPoints[11].position.x
      val leftleg1y= person.keyPoints[13].position.y-person.keyPoints[11].position.y
      val leftleg2x= person.keyPoints[15].position.x-person.keyPoints[13].position.x
      val leftleg2y= person.keyPoints[15].position.y-person.keyPoints[13].position.y

      //right leg
      val rightleg1x= person.keyPoints[14].position.x-person.keyPoints[12].position.x
      val rightleg1y= person.keyPoints[14].position.y-person.keyPoints[12].position.y
      val rightleg2x= person.keyPoints[16].position.x-person.keyPoints[14].position.x
      val rightleg2y= person.keyPoints[16].position.y-person.keyPoints[14].position.y


      // 미션
      /*
      * private val targett1= listOf(
    Pair(cos(50.toFloat()),-sin(50.toFloat())),//Pair(119,-113),
    Pair(168,-80),
    Pair(-cos(50.toFloat()),-sin(50.toFloat())),//Pair(-84,-105),
    Pair(67,-105)
  )
  private val targett2= listOf(
    Pair(-cos(50.toFloat()),-sin(50.toFloat()))
  )

  private val targett3= listOf(
    Pair(cos(50.toFloat()),-sin(50.toFloat()))
  )

  private val targett4= listOf(
    Pair(-199.61085,  -132.37355)
  )
      * */


        when (stage) {
          //두팔위로
          1 -> {
            var simil1 = getcos(
              cos(50*rad.toFloat()),
              -sin(50*rad.toFloat()),
              leftarm1x.toFloat(),
              leftarm1y.toFloat()
            )


            var simil2 = getcos(
              -cos(50*rad.toFloat()),-sin(50*rad.toFloat()),
              rightarm1x.toFloat(),
              rightarm1y.toFloat()
            )
            print("코  ")
            println(simil1.toString())
            print("싸  ")
            println(simil2.toString())

//
            var simil12 = getcos(
              119.toFloat(),(-113).toFloat(),
              leftarm1x.toFloat(),
              leftarm1y.toFloat()
            )


            var simil22 = getcos(
              ( -84).toFloat(),(-105).toFloat(),
              rightarm1x.toFloat(),
              rightarm1y.toFloat()
            )
            print("코2  ")
            println(simil12.toString())
            print("싸2  ")
            println(simil22.toString())

            var simil13 = getcos(
              cos(45*rad.toFloat()),
              -sin(45*rad.toFloat()),
              leftarm1x.toFloat(),
              leftarm1y.toFloat()
            )


            var simil23= getcos(
              -cos(45*rad.toFloat()),-sin(45*rad.toFloat()),
              rightarm1x.toFloat(),
              rightarm1y.toFloat()
            )

            //
            if (simil1 < 0.52 && simil2 < 0.52) {
              imageView1?.visibility = View.INVISIBLE
              stage = 2
              stepProgressView?.currentProgress=1
              val activity = activity
              activity?.runOnUiThread { imageView2?.visibility = View.VISIBLE }

            }
            val simill=(simil1+simil2)/2
            showscore=((1.5-simill)*100).toFloat()

          }
          //오른쪽 굽히기
          2 -> {
            val NoseRighthipx = person.keyPoints[0].position.x-person.keyPoints[12].position.x
            val NoseRighthipy = person.keyPoints[0].position.y-person.keyPoints[12].position.y
            var simil3 = getcos( -cos(50*rad.toFloat()),-sin(50*rad.toFloat()), NoseRighthipx.toFloat(), NoseRighthipy.toFloat())

            if(simil3<0.52){
              imageView2?.visibility = View.INVISIBLE
              stage = 3
              stepProgressView?.currentProgress=2
              val activity = activity
              activity?.runOnUiThread { imageView3?.visibility = View.VISIBLE }
            }
            showscore=((1.5-simil3)*100).toFloat()

          }
          //왼쪽굽히기
          3 -> {
            val NoseLefthipx = person.keyPoints[0].position.x-person.keyPoints[11].position.x
            val NoseLefthipy = person.keyPoints[0].position.y-person.keyPoints[11].position.y
            var simil3 = getcos( cos(50*rad.toFloat()),-sin(50*rad.toFloat()),
              NoseLefthipx.toFloat(),
              NoseLefthipy.toFloat()
            )
            if(simil3<0.52){
              imageView3?.visibility = View.INVISIBLE
              stage = 4
              stepProgressView?.currentProgress=3
              val activity = activity
              activity?.runOnUiThread { imageView4?.visibility = View.VISIBLE }
            }
            showscore=((1.5-simil3)*100).toFloat()


          }
          //스쿼트
          4 -> {

            var simil4= getcos(
              -cos(15*rad.toFloat()),sin(15*rad.toFloat()),
              rightarm1x.toFloat(),
              rightarm1y.toFloat()
            )
            var simil5= getcos(
              -cos(15*rad.toFloat()),sin(15*rad.toFloat()),
              leftleg1x.toFloat(),
              leftleg1y.toFloat()
            )
            var simil6 = getcos(
              0.toFloat(),
              1.toFloat(),
              leftleg2x.toFloat(),
              leftleg2y.toFloat()
            )
            if(simil4<0.52 && simil5<0.52){
              imageView4?.visibility = View.INVISIBLE
              stage = 5
              stepProgressView?.currentProgress=4

              print("팔 ")
              println(simil4.toString())
              print("다리 ")
              println(simil5.toString())


          }
            val simill=(simil4+simil5)/2
            showscore=((1.5-simill)*100).toFloat()

          }
          else ->{
            // Create fragment and give it an argument specifying the article it should show

          if(!isshow) {
            val builder = AlertDialog.Builder(this.context)
            builder.setTitle("알람이 종료되었습니다.")
            builder.setMessage("잠이 깨셨습니까?")
            builder.setPositiveButton("네") { dialog, which ->
              // Toast.makeText(this.context?.applicationContext, "알람을 종료합니다",Toast.LENGTH_SHORT).show()
              this.activity?.finish()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
              // Toast.makeText(this.context?.applicationContext,"다시 1단계를 시작합니다",Toast.LENGTH_SHORT).show()

              activity?.finish()

              startActivity(this.activity?.intent)

            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
            isshow=true


          }


          }


      }
      if (showscore<0) showscore=0f
      else if (showscore>100) showscore=100.0f

    }
    else{ showscore =0f}
    if(stage<5) {

    canvas.drawText(
      "인식률: %s".format((person.score*100).toInt()),
      (15.0f * widthRatio),
      (30.0f * heightRatio + bottom),
      paint
    )

      canvas.drawText(
        "점수: %s".format(showscore.toInt()),
        (15.0f * widthRatio),
        (50.0f * heightRatio + bottom),
        paint
      )
    }
/*
    canvas.drawText(
      "Time: %.2f ms".format(posenet.lastInferenceTimeNanos * 1.0f / 1_000_000),
      (15.0f * widthRatio),
      (70.0f * heightRatio + bottom),
      paint
    )
*/
    // Draw!
    surfaceHolder!!.unlockCanvasAndPost(canvas)
  }



  /** Process image using Posenet library.   */
  private fun processImage(bitmap: Bitmap) {
    // Crop bitmap.
    val croppedBitmap = cropBitmap(bitmap)

    // Created scaled version of bitmap for model input.
    val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true)

    // Perform inference.
    val person = posenet.estimateSinglePose(scaledBitmap)
    val canvas: Canvas = surfaceHolder!!.lockCanvas()
    draw(canvas, person, scaledBitmap)


  }


  /**
   * Creates a new [CameraCaptureSession] for camera preview.
   */
  private fun createCameraPreviewSession() {
    try {

      // We capture images from preview in YUV format.
      imageReader = ImageReader.newInstance(
        previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 2
      )
      imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

      // This is the surface we need to record images for processing.
      val recordingSurface = imageReader!!.surface

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice!!.createCaptureRequest(
        CameraDevice.TEMPLATE_PREVIEW
      )
      previewRequestBuilder!!.addTarget(recordingSurface)

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice!!.createCaptureSession(
        listOf(recordingSurface),
        object : CameraCaptureSession.StateCallback() {
          override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            // The camera is already closed
            if (cameraDevice == null) return

            // When the session is ready, we start displaying the preview.
            captureSession = cameraCaptureSession
            try {
              // Auto focus should be continuous for camera preview.
              previewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
              )
              // Flash is automatically enabled when necessary.
              setAutoFlash(previewRequestBuilder!!)

              // Finally, we start displaying the camera preview.
              previewRequest = previewRequestBuilder!!.build()
              captureSession!!.setRepeatingRequest(
                previewRequest!!,
                captureCallback, backgroundHandler
              )
            } catch (e: CameraAccessException) {
              Log.e(TAG, e.toString())
            }
          }

          override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            showToast("Failed")
          }
        },
        null
      )
    } catch (e: CameraAccessException) {
      Log.e(TAG, e.toString())
    }
  }

  private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
    if (flashSupported) {
      requestBuilder.set(
        CaptureRequest.CONTROL_AE_MODE,
        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
      )
    }
  }

  /**
   * Shows an error message dialog.
   */
  class ErrorDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
      AlertDialog.Builder(activity)
        .setMessage(arguments!!.getString(ARG_MESSAGE))
        .setPositiveButton(android.R.string.ok) { _, _ -> activity!!.finish() }
        .create()

    companion object {

      @JvmStatic
      private val ARG_MESSAGE = "message"

      @JvmStatic
      fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
        arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
      }
    }
  }

  companion object {
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private val ORIENTATIONS = SparseIntArray()
    private val FRAGMENT_DIALOG = "dialog"

    init {
      ORIENTATIONS.append(Surface.ROTATION_0, 90)
      ORIENTATIONS.append(Surface.ROTATION_90, 180)
      ORIENTATIONS.append(Surface.ROTATION_180, 270)
      ORIENTATIONS.append(Surface.ROTATION_270, 0)
    }

    /**
     * Tag for the [Log].
     */
    private const val TAG = "PosenetActivity"
  }
}
