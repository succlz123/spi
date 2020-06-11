package org.succlz123.spi.app.impl;

import android.util.Log;

import org.succlz123.spi.SpiImpl;
import org.succlz123.spi.app.service.IPlayer;

@SpiImpl(name = "MusicPlayer")
public class MusicPlayer implements IPlayer {

    @Override
    public void play() {
        Log.e("Spi", "MusicPlayer is play");
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
