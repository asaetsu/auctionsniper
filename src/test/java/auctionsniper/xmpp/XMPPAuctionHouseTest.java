package auctionsniper.xmpp;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.end2end.ApplicationRunner;
import auctionsniper.end2end.FakeAuctionServer;

public class XMPPAuctionHouseTest {
    private static final String itemId = "item-54321";
    private static final String SNIPER_XMPP_ID = String.format(
            ApplicationRunner.SNIPER_XMPP_ID_FORMAT, itemId);
    private FakeAuctionServer auctionServer;
    private XMPPAuctionHouse auctionHouse;

    @Before
    public void startAuctionServer() throws Exception {
        auctionServer = new FakeAuctionServer(itemId);
        auctionServer.startSellingItem();
    }

    @Before
    public void openConnection() throws Exception {
        auctionHouse = XMPPAuctionHouse.connect(
                FakeAuctionServer.XMPP_HOST_NAME, FakeAuctionServer.XMPP_PORT,
                FakeAuctionServer.XMPP_DOMAIN_NAME,
                ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD,
                itemId);
    }

    @After
    public void closeConnection() {
        if (auctionHouse != null) {
            auctionHouse.disconnect();
        }
    }

    @After
    public void stopAuctionServer() {
        if (auctionServer != null) {
            auctionServer.stop();
        }
    }

    @Test
    public void receivesEventsFromAuctionServerAfterJoining() throws Exception {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);

        Auction auction = auctionHouse.auctionFor(auctionServer.getItemId());
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));

        auction.join();
        auctionServer.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);
        auctionServer.announceClosed();

        assertThat("should have been closed",
                auctionWasClosed.await(4, SECONDS), is(true));
    }

    private AuctionEventListener auctionClosedListener(
            CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            @Override
            public void currentPrice(int price, int increment,
                    PriceSource priceSource) {
                // 未実装
            }
        };
    }
}
