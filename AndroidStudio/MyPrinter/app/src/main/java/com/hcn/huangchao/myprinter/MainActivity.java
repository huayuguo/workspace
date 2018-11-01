package com.hcn.huangchao.myprinter;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button myButton = (Button)findViewById(R.id.button1);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPdfPrint(Environment.getExternalStorageDirectory() + "/test.pdf");
            }
        });

        Button myButton2 = (Button)findViewById(R.id.button2);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void doPdfPrint(String filePath) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        MyPrintPdfAdapter myPrintAdapter = new MyPrintPdfAdapter(filePath);
        printManager.print("jobName", myPrintAdapter, null);
    }

    public class MyPrintPdfAdapter extends PrintDocumentAdapter {
        private String mFilePath;

        public MyPrintPdfAdapter(String file) {
            this.mFilePath = file;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }
            PrintDocumentInfo info = new PrintDocumentInfo.Builder("name")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                            CancellationSignal cancellationSignal,
                            WriteResultCallback callback) {
            InputStream input = null;
            OutputStream output = null;

            try {
                input = new FileInputStream(mFilePath);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
