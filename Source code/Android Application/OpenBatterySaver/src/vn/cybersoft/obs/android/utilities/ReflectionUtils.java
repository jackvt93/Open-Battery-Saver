/*
 * Copyright (C) 2014 IUH €yber$oft Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vn.cybersoft.obs.android.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * @author Luan Vu
 *
 */
public class ReflectionUtils {
	
	public static int getResourceId(String variableName, Class<?> c) {
		//I just found a blog post saying that Resources.getIdentifier() is slower than using reflection
		// like I did. Check it out: http://daniel-codes.blogspot.com/2009/12/dynamically-retrieving-resources-in.html.
		try {
			Field idField = c.getField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			// no field found
			return -1;
		} 
	}
	
	public static Constructor<?> getClassConstructor(String packageName, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
		return Class.forName(packageName).getConstructor(parameterTypes); 
	}
	
	public static Field getClassField(String packageName, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
		return getClass(packageName).getField(fieldName); 
	}
	
	public static Method getClassMethod(String packageName, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
		return Class.forName(packageName).getMethod(methodName, parameterTypes);
	}
	
	public static Class<?> getClass(String packageName) throws ClassNotFoundException { 
		return Class.forName(packageName);
	}
	
	
	
}
