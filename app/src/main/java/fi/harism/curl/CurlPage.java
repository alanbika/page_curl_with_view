/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package fi.harism.curl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Storage class for page textures, blend colors and possibly some other values
 * in the future.
 * 
 * @author harism
 */
public class CurlPage {

	public static final int SIDE_BACK = 2;
	public static final int SIDE_BOTH = 3;
	public static final int SIDE_FRONT = 1;

	private int mColorBack;
	private int mColorFront;
	private Bitmap mTextureBack;
	private Bitmap mTextureFront;
	private boolean mTexturesChanged;
	private Bitmap mTextureFrontWithViews;
	private Bitmap mTextureFrontWithoutViews;
	/**
	 * Default constructor.
	 */
	public CurlPage() {
		reset();
	}

	/**
	 * Getter for color.
	 */
	public int getColor(int side) {
		switch (side) {
		case SIDE_FRONT:
			return mColorFront;
		default:
			return mColorBack;
		}
	}

	/**
	 * Calculates the next highest power of two for a given integer.
	 */
	private int getNextHighestPO2(int n) {
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		n = n | (n >> 32);
		return n + 1;
	}

	/**
	 * Generates nearest power of two sized Bitmap for give Bitmap. Returns this
	 * new Bitmap using default return statement + original texture coordinates
	 * are stored into RectF.
	 */
	private Bitmap getTexture(Bitmap bitmap, RectF textureRect) {
		// Bitmap original size.
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		// Bitmap size expanded to next power of two. This is done due to
		// the requirement on many devices, texture width and height should
		// be power of two.
		int newW = getNextHighestPO2(w);
		int newH = getNextHighestPO2(h);
		// TODO: Is there another way to create a bigger Bitmap and copy
		// original Bitmap to it more efficiently? Immutable bitmap anyone?
		Bitmap bitmapTex = Bitmap.createBitmap(newW, newH, bitmap.getConfig());
		Canvas c = new Canvas(bitmapTex);
		c.drawBitmap(bitmap, 0, 0, null);
		/*if (parentLayoutBmp!=null){
			Log.e("~~~~~~~~~~","~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File pic = new File(path,"tem15210.jpg");

			BufferedOutputStream bos = null;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(pic));
				parentLayoutBmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			c.drawBitmap(parentLayoutBmp , 0, 0, null);

		}*/
		// Calculate final texture coordinates.
		float texX = (float) w / newW;
		float texY = (float) h / newH;
		textureRect.set(0f, 0f, texX, texY);
		return bitmapTex;
	}

	/**
	 * Getter for textures. Creates Bitmap sized to nearest power of two, copies
	 * original Bitmap into it and returns it. RectF given as parameter is
	 * filled with actual texture coordinates in this new upscaled texture
	 * Bitmap.
	 */
	public Bitmap getTexture(RectF textureRect, int side) {
		switch (side) {
		case SIDE_FRONT:
			return getTexture(mTextureFront, textureRect);

		default:
			return getTexture(mTextureBack, textureRect);
		}
	}

	/**
	 * Returns true if textures have changed.
	 */
	public boolean getTexturesChanged() {
		return mTexturesChanged;
	}

	/**
	 * Returns true if back siding texture exists and it differs from front
	 * facing one.
	 */
	public boolean hasBackTexture() {
		return !mTextureFront.equals(mTextureBack);
	}

	/**
	 * Recycles and frees underlying Bitmaps.
	 */
	public void recycle() {
		if (mTextureFront != null) {
			mTextureFront.recycle();
		}
		mTextureFront = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
		mTextureFront.eraseColor(mColorFront);
		if (mTextureBack != null) {
			mTextureBack.recycle();
		}
		mTextureBack = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
		mTextureBack.eraseColor(mColorBack);
		mTexturesChanged = false;
	}

	/**
	 * Resets this CurlPage into its initial state.
	 */
	public void reset() {
		mColorBack = Color.WHITE;
		mColorFront = Color.WHITE;
		recycle();
	}

	/**
	 * Setter blend color.
	 */
	public void setColor(int color, int side) {
		switch (side) {
		case SIDE_FRONT:
			mColorFront = color;
			break;
		case SIDE_BACK:
			mColorBack = color;
			break;
		default:
			mColorFront = mColorBack = color;
			break;
		}
	}

	/**
	 * Setter for textures.
	 */
	public void setTexture(Bitmap texture, int side) {
		if (texture == null) {
			texture = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
			if (side == SIDE_BACK) {
				texture.eraseColor(mColorBack);
			} else {
				texture.eraseColor(mColorFront);
			}
		}
		switch (side) {
		case SIDE_FRONT:
			if (mTextureFront != null)
				mTextureFront.recycle();
			mTextureFront = texture;
			break;
		case SIDE_BACK:
			if (mTextureBack != null)
				mTextureBack.recycle();
			mTextureBack = texture;
			break;
		case SIDE_BOTH:
			if (mTextureFront != null)
				mTextureFront.recycle();
			if (mTextureBack != null)
				mTextureBack.recycle();
			mTextureFront = mTextureBack = texture;
			break;
		}
		mTextureFrontWithoutViews = mTextureFront;
		mTexturesChanged = true;
	}
	public void changeWithViews(Bitmap parentLayoutBmp) {
		if (mTextureFrontWithViews == null){

			mTextureFrontWithViews = mTextureFrontWithoutViews.copy(Bitmap.Config.RGB_565, true);
			Canvas c = new Canvas(mTextureFrontWithViews);
			c.drawBitmap(parentLayoutBmp , 0, 0, null);
 			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File pic = new File(path,"mTextureFrontWithViews.jpg");

			BufferedOutputStream bos = null;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(pic));
				mTextureFrontWithViews.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		mTextureFront = mTextureFrontWithViews.copy(Bitmap.Config.RGB_565,true);
		mTexturesChanged = true;
	}
	public void changeWithoutViews() {

		mTextureFront = mTextureFrontWithoutViews.copy(Bitmap.Config.RGB_565,true);
		mTexturesChanged = true;
	}
	public void setmTextureFrontWithoutViews(){
		mTextureFrontWithoutViews = mTextureFront.copy(Bitmap.Config.RGB_565,true);
		/*File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File pic = new File(path,"10.jpg");

		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(pic));
			mTextureFrontWithoutViews.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}
