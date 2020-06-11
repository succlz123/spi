package org.succlz123.spi.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.succlz123.spi.SpiManager;
import org.succlz123.spi.app.service.IBookService;
import org.succlz123.spi.app.service.IPlayer;
import org.succlz123.spi.app.test.service.ITestService;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.content);
        // 单例 - 单例
        // 单例 - 非单例
        try {
            Log.e("Spi", "======= main module =======");

            IPlayer player0 = SpiManager.instance().optSingleByName(IPlayer.class, "AvPlayer");
            player0.play();

            IPlayer player = SpiManager.instance().opt(IPlayer.class);
            player.play();

            IPlayer musicPlayer = SpiManager.instance().optByName(IPlayer.class, "MusicPlayer");
            musicPlayer.play();
            IPlayer tvPlayer = SpiManager.instance().optByName(IPlayer.class, "TvPlayer");
            tvPlayer.play();
            List<IPlayer> players = SpiManager.instance().optAll(IPlayer.class);
            for (IPlayer iPlayer : players) {
                iPlayer.play();
            }

            Log.e("Spi", "======= single =======");

            IBookService iBookService1 = SpiManager.instance().optSingle(IBookService.class);
            iBookService1.start();
            IBookService iBookService2 = SpiManager.instance().optSingleByName(IBookService.class, "SingleBook");
            iBookService2.start();
            IBookService iBookService3 = SpiManager.instance().optSingle(IBookService.class);
            iBookService3.start();
            try {
                IBookService iBookService4 = SpiManager.instance().optSingleByName(IBookService.class, "SingleBook_Test");
                iBookService4.start();
            } catch (Exception e) {
                Log.e("spi", e.toString());
            }

            Log.e("Spi", "======= other module =======");

            ITestService testService = SpiManager.instance().opt(ITestService.class);
            testService.test();
            ITestService mainTestService = SpiManager.instance().optByName(ITestService.class, "Main");
            mainTestService.test();
        } catch (Exception e) {
            Log.e("spi", e.toString());
        }
    }
}
