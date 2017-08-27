package auctionsniper;

import static auctionsniper.AuctionEventListener.PriceSource.*;

public class AuctionSniper implements AuctionEventListener {
    private SniperListener listener;
    private Auction auction;
    private boolean isWinning;
    private String itemId;

    public AuctionSniper(String itemId, Auction auction, SniperListener listener) {
        this.auction = auction;
        this.listener = listener;
        this.itemId = itemId;
    }

    @Override
    public void auctionClosed() {
        if (isWinning) {
            listener.sniperWon();
        } else {
            listener.sniperLost();
        }
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        isWinning = priceSource == FromSniper;
        if (isWinning) {
            listener.sniperWinning();
        } else {
            int bid = price + increment;
            auction.bid(bid);
            listener.sniperBidding(new SniperState(itemId, price, bid));
        }
    }
}
