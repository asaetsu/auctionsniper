package auctionsniper;

import static auctionsniper.AuctionEventListener.PriceSource.*;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

public class AuctionSniperTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final States sniperStates = context.states("sniper");
    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context
            .mock(SniperListener.class);
    String ITEM_ID = "itemId";
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction,
            sniperListener);

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        context.checking(new Expectations() {
            {
                oneOf(sniperListener).sniperLost();
            }
        });

        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;
        context.checking(new Expectations() {
            {
                oneOf(auction).bid(bid);
                atLeast(1).of(sniperListener).sniperBidding(
                        new SniperState(ITEM_ID, price, bid));
            }
        });

        sniper.currentPrice(price, increment, FromOtherBidder);// `PriceSource`
                                                               // はどちらでも OK.
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperWinning();
            }
        });
        sniper.currentPrice(123, 45, FromSniper);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;
        context.checking(new Expectations() {
            {
                ignoring(auction);

                // allowing(sniperListener).sniperBidding(
                // new SniperState(ITEM_ID, price, bid));

                allowing(sniperListener).sniperBidding(
                        with(any(SniperState.class)));
                then(sniperStates.is("bidding"));

                atLeast(1).of(sniperListener).sniperLost();
                when(sniperStates.is("bidding"));
            }
        });

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        context.checking(new Expectations() {
            {
                ignoring(auction);

                allowing(sniperListener).sniperWinning();
                then(sniperStates.is("winning"));

                atLeast(1).of(sniperListener).sniperWon();
                when(sniperStates.is("winning"));
            }
        });

        sniper.currentPrice(123, 45, FromSniper);
        sniper.auctionClosed();
    }
}