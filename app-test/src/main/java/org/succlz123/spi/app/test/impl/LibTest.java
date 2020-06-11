package org.succlz123.spi.app.test.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.app.test.service.ITestService;

@SpiImpl()
public class LibTest implements ITestService {

    @Override
    public void test() {
        Log.e("spi", "LibTest is test");
    }
}
