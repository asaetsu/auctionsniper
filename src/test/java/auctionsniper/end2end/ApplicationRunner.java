package auctionsniper.end2end;

import ui.MainWindow;
import auctionsniper.Main;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    private AuctionSniperDriver driver;

    public void startBiddingIn(FakeAuctionServer auction) {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                Main.main(FakeAuctionServer.XMPP_HOST_NAME, SNIPER_ID,
                        SNIPER_PASSWORD, auction.getItemId());
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver = new AuctionSniperDriver(1000);
        driver.showsSniperStatus(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        driver.showsSniperStatus(MainWindow.SNIPER_STATUS_NAME);
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }
}
