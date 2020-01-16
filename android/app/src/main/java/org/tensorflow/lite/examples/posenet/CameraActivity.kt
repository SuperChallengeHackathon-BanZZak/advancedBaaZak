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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class CameraActivity : AppCompatActivity() {


   // val bluetoothKit = BluetoothKit()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_camera)

    // val kotlinFragment = PosenetActivity.newInstance("Hello", 111, testData)



//     val kotlinFragment = KotlinFragment.newInstance("Hello", 111, testData)
//
//     supportFragmentManager
//       .beginTransaction()
//       .replace(R.id.content, kotlinFragment, fragment.KotlinFragment.javaClass.name)
//       .commit()

     val bundle = Bundle()

     val id = intent.getIntExtra("alarm_id", 0)
     bundle.putInt("alarm_id", id)

     val fragInfo = PosenetActivity()
     fragInfo.setArguments(bundle)

    savedInstanceState ?: supportFragmentManager.beginTransaction()
      .replace(R.id.container, fragInfo)
      .commit()
  }

}
