package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.SpiManager;
import org.succlz123.spi.SpiSingleService;
import org.succlz123.spi.app.service.IPlayer;
import org.succlz123.spi.app.service.IWifiService;

@SpiImpl(name = "AvPlayer")
public class AvPlayer implements IPlayer, SpiSingleService {
    IWifiService wifiService;

    @Override
    public void initField() {
        wifiService = SpiManager.instance().opt(IWifiService.class);
    }

    @Override
    public void play() {
        Log.e("Spi", "AvPlayer is play");
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void seek(int time) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }
}
