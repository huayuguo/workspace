package com.camera.simplewebcam;

import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;

public class AsianFontProvider extends XMLWorkerFontProvider {
	
	private static final String TAG = "AsianFontProvider";

    @Override
    public Font getFont(final String fontname, final String encoding,
			final boolean embedded, final float size, final int style,
			final BaseColor color) {
        BaseFont bf = null;
        Log.d(TAG, "getFont 32");
        try {
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Log.d(TAG, "getFont 35");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Font font = new Font(bf, size, style, color);
        font.setColor(color);
        return font;
    }
}