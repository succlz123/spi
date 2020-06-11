package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.app.service.IPlayer;

@SpiImpl(name = "TvPlayer")
public class TvPlayer implements IPlayer {
    int count = 1;

    @Override
    public void play() {
        Log.e("Spi", "TvPlayer is play " + count);
        count++;
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
