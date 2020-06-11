package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.app.service.IBookService;

@SpiImpl(name = "SingleBook")
public class SingleBook implements IBookService {
    int count = 1;

    @Override
    public void start() {
        Log.e("Spi", "SingleBook is start " + count);
        count++;
    }

    @Override
    public void close() {

    }
}
