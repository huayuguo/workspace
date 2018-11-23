/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.yjzn;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class Em3096Native {

	private static final String TAG = "Em3096";

	public  Em3096Native(){
		
	}
	public void startScan() {
		start_scan();
	}

	public void stopScan() {
		stop_scan();
	}
	public void powerOnOff(int poweron) {
		power(poweron);
	}
	// JNI
	public native void power(int poweron);
	public native void start_scan();
	public native void stop_scan();
	static {
		System.loadLibrary("factory_test_jni");
	}
}
