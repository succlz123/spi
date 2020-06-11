package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.app.service.IImageTask;

@SpiImpl(name = "ImageTask")
public class ImageTask implements IImageTask {

    @Override
    public void display() {
        Log.e("Spi", "ImageTask is display");
    }
}
