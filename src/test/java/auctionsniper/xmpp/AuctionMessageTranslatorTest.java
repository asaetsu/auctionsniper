package auctionsniper.xmpp;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.jxmpp.jid.EntityBareJid;

import auctionsniper.AuctionEventListener;

public class AuctionMessageTranslatorTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private static final String SNIPER_XMPP_ID = "sniper xmpp id";
    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid UNUSED_JID = null;
    private final AuctionEventListener listener = context
            .mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(
            SNIPER_XMPP_ID, listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        context.checking(new Expectations() {
            {
                oneOf(listener).auctionClosed();
            }
        });

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        int price = 192;
        int increment = 7;
        context.checking(new Expectations() {
            {
                oneOf(listener)
                        .currentPrice(
                                price,
                                increment,
                                auctionsniper.AuctionEventListener.PriceSource.FromOtherBidder);
            }
        });

        Message message = new Message();
        message.setBody(String
                .format("SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: Someoene else;",
                        price, increment));

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        int price = 234;
        int increment = 5;
        context.checking(new Expectations() {
            {
                oneOf(listener)
                        .currentPrice(
                                price,
                                increment,
                                auctionsniper.AuctionEventListener.PriceSource.FromSniper);
            }
        });

        Message message = new Message();
        message.setBody(String
                .format("SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",
                        price, increment, SNIPER_XMPP_ID));

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }
}